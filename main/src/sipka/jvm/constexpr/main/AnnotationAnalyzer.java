package sipka.jvm.constexpr.main;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.annotations.Deconstructor;
import sipka.jvm.constexpr.tool.ConstantExpressionInliner;
import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.log.ConfigClassMemberInaccessibleLogEntry;
import sipka.jvm.constexpr.tool.log.MessageLogEntry;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.AnnotationVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.FieldVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.MethodVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class AnnotationAnalyzer {

	private AnnotationAnalyzer() {
		throw new UnsupportedOperationException();
	}

	public static void analyzeClassFile(InlinerOptions options, Iterable<? extends byte[]> classbytes)
			throws Exception {
		analyzeClassFile(options, classbytes, options.getClassLoader());
	}

	public static void analyzeClassFile(InlinerOptions options, Iterable<? extends byte[]> classbytes, ClassLoader cl)
			throws Exception {
		Map<Class<?>, DeconstructorSettings> deconstructorSettings = new HashMap<>();
		for (byte[] bytes : classbytes) {
			analyzeClassFile(options, bytes, cl, deconstructorSettings);
		}

		for (Entry<Class<?>, DeconstructorSettings> entry : deconstructorSettings.entrySet()) {
			DeconstructorSettings settings = entry.getValue();
			DeconstructionSelector selector = settings.toDeconstructionSelector();
			if (selector == null) {
				//not configured, only the settings instance is present
				continue;
			}
			options.getDeconstructorConfigurations().put(entry.getKey(), selector);
		}
	}

	public static Collection<? extends Method> searchGetter(Class<?> type, Class<?> paramtype, String paramname) {
		Map<NameDescriptor, Method> methods = new TreeMap<>();
		searchGetterImpl(type, paramtype, paramname.toLowerCase(Locale.ROOT), methods);
		return methods.values();
	}

	private static void analyzeClassFile(InlinerOptions options, byte[] bytes, ClassLoader cl,
			Map<Class<?>, DeconstructorSettings> deconstructorSettings) throws Exception {
		ClassReader cr = new ClassReader(bytes);
		analyzeClassFile(options, cr, cl, deconstructorSettings);
	}

	private static void analyzeClassFile(InlinerOptions options, ClassReader cr, ClassLoader cl,
			Map<Class<?>, DeconstructorSettings> deconstructorSettings)
			throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
		AnalyzerClassVisitor analyzer = new AnalyzerClassVisitor(ConstantExpressionInliner.ASM_API);
		cr.accept(analyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		if (!analyzer.isAnyConstantRelatedSettings()) {
			return;
		}
		Class<?> type = Class.forName(Type.getObjectType(analyzer.classInternalName).getClassName(), false, cl);
		if (analyzer.constantExpression) {
			options.getConstantTypes().add(type);
		}
		if (analyzer.enumType) {
			//add the fields as enum reconstructors
			for (Field field : type.getDeclaredFields()) {
				if (field.isEnumConstant()) {
					options.getConstantReconstructors().put(field, ReconstructorPredicate.ALLOW_ALL);
				}
			}
			try {
				Method valueofmethod = type.getMethod("valueOf", String.class);
				options.getConstantReconstructors().put(valueofmethod, ReconstructorPredicate.ALLOW_ALL);
			} catch (NoSuchMethodException e) {
				// this method should be present for all enum types
				// log this, as it is a serious unexpected error (even if this valueOf method is not directly used by the optimizer)
				options.getLogger().log(new ConfigClassMemberInaccessibleLogEntry(analyzer.classInternalName, "valueOf",
						"(Ljava/lang/String;)L" + analyzer.classInternalName + ";", e));
			}

		}
		for (NameDescriptor namedescriptor : analyzer.constantExpressionFields) {
			Field member = Utils.getFieldForDescriptor(type, namedescriptor.name, namedescriptor.descriptor);
			if (((member.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				options.getConstantFields().add(member);
			} else {
				options.getConstantReconstructors().put(member, ReconstructorPredicate.ALLOW_ALL);
			}
		}
		for (NameDescriptor namedescriptor : analyzer.constantExpressionMethods) {
			Method member = Utils.getMethodForMethodDescriptor(type, analyzer.classInternalName,
					namedescriptor.descriptor, namedescriptor.name);
			ReconstructorPredicate predicate;
			if (Modifier.isStatic(member.getModifiers())) {
				predicate = ReconstructorPredicate.ALLOW_ALL;
			} else {
				//in case of instance methods
				//only allow calling the annotated methods on the type itself, or on its subclasses
				predicate = ReconstructorPredicate.allowInstanceOf(analyzer.classInternalName);
			}
			options.getConstantReconstructors().put(member, predicate);
		}

		List<Field> equalityfields = new ArrayList<>(analyzer.deconstructorFields.size());
		for (NameDescriptor namedescriptor : analyzer.deconstructorFields) {
			Field field = Utils.getFieldForDescriptor(type, namedescriptor.name, namedescriptor.descriptor);
			if (field.isEnumConstant()) {
				//ignore, always used regardless of configuration
				options.getLogger()
						.log(new MessageLogEntry("Redundant annodation of @" + Deconstructor.class.getSimpleName()
								+ " on enum field: " + Type.getObjectType(analyzer.classInternalName).getClassName()
								+ "." + namedescriptor.name));
				continue;
			}
			if (!((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				options.getLogger()
						.log(new MessageLogEntry("Ignoring non-static field marked with @"
								+ Deconstructor.class.getSimpleName() + " annotation: "
								+ Utils.memberDescriptorToPrettyString(Type.getType(namedescriptor.descriptor),
										Type.getObjectType(analyzer.classInternalName), namedescriptor.name)));
				continue;
			}
			equalityfields.add(field);
			deconstructorSettings.computeIfAbsent(field.getType(), x -> new DeconstructorSettings()).addField(field);
		}

		if (analyzer.deconstructorMethods.size() > 1) {
			throw new IllegalArgumentException(
					"Multiple method deconstructors specified for: " + analyzer.classInternalName);
		}
		//TODO support multiple executable deconstructors
		for (NameDescriptor namedescriptor : analyzer.deconstructorMethods) {
			Executable e = Utils.getExecutableForDescriptor(type, analyzer.classInternalName, namedescriptor.name,
					namedescriptor.descriptor);
			java.lang.reflect.Parameter[] params = e.getParameters();
			DeconstructionDataAccessor[] parameterdataaccessors = new DeconstructionDataAccessor[params.length];
			for (int i = 0; i < parameterdataaccessors.length; i++) {
				java.lang.reflect.Parameter param = params[i];
				Class<?> paramtype = param.getType();
				String paramname = param.getName();
				//search getter for paramname, that has a return type of paramtype
				Collection<? extends Method> getters = searchGetter(type, paramtype, paramname);
				if (getters.isEmpty()) {
					throw new IllegalArgumentException("Getter not found for deconstructor: " + e + " with parameter["
							+ i + "] " + paramtype + " " + paramname);
				}
				if (getters.size() != 1) {
					throw new IllegalArgumentException("Multiple getters found for deconstructor: " + e
							+ " with parameter[" + i + "] " + paramtype + " " + paramname + ": " + getters);
				}
				Method gettermethod = getters.iterator().next();
				parameterdataaccessors[i] = DeconstructionDataAccessor.createForMethod(gettermethod);
			}
			deconstructorSettings
					.computeIfAbsent(Utils.getExecutableEffectiveReturnType(e), x -> new DeconstructorSettings())
					.addExecutable(DeconstructorConfiguration.createExecutable(e, parameterdataaccessors));
		}
	}

	private static void searchGetterImpl(Class<?> type, Class<?> paramtype, String paramname,
			Map<NameDescriptor, ? super Method> methods) {
		if (type == null) {
			return;
		}
		for (Method m : type.getDeclaredMethods()) {
			if (m.getReturnType() != paramtype || m.getParameterCount() != 0) {
				continue;
			}
			if (isGetterNameFunction(paramname, m.getName())) {
				methods.putIfAbsent(new NameDescriptor(m.getName(), Type.getMethodDescriptor(m)), m);
			}
		}
		searchGetterImpl(type.getSuperclass(), paramtype, paramname, methods);
		for (Class<?> itf : type.getInterfaces()) {
			searchGetterImpl(itf, paramtype, paramname, methods);
		}
	}

	private static boolean isGetterNameFunction(String parameternamelowercase, String methodname) {
		methodname = methodname.toLowerCase(Locale.ROOT);
		if (methodname.equals(parameternamelowercase)) {
			return true;
		}
		if (methodname.startsWith("get")) {
			if (methodname.length() == 3 + parameternamelowercase.length()
					&& methodname.regionMatches(3, parameternamelowercase, 0, parameternamelowercase.length())) {
				return true;
			}
		}
		return false;
	}

	private static final class DeconstructorSettings {
		private Collection<Field> equalityFields = new LinkedHashSet<>();
		private Collection<DeconstructorConfiguration> executableConfigurations = new LinkedHashSet<>();

		public DeconstructionSelector toDeconstructionSelector() {
			if (executableConfigurations.size() > 1) {
				throw new IllegalArgumentException("Too many executable deconstructors: " + executableConfigurations);
			}
			DeconstructionSelector execdecons;
			if (!executableConfigurations.isEmpty()) {
				execdecons = DeconstructionSelector.getForConfiguration(executableConfigurations.iterator().next());
			} else {
				execdecons = null;
			}
			return DeconstructionSelector.getMultiSelector(
					DeconstructionSelector.getStaticFieldEquality(equalityFields.toArray(new Field[0])), execdecons);
		}

		void addField(Field field) {
			equalityFields.add(field);

		}

		void addExecutable(DeconstructorConfiguration execconfig) {
			executableConfigurations.add(execconfig);
		}
	}

	private static final class AnalyzerClassVisitor extends ClassVisitor {
		protected static final String CONSTANTEXPRESSION_DESCRIPTOR = Type.getDescriptor(ConstantExpression.class);
		protected static final String DECONSTRUCTOR_DESCRIPTOR = Type.getDescriptor(Deconstructor.class);

		protected String classInternalName;
		protected boolean enumType;
		protected boolean constantExpression;

		protected Set<NameDescriptor> constantExpressionMethods = new TreeSet<>();
		protected Set<NameDescriptor> constantExpressionFields = new TreeSet<>();
		protected Set<NameDescriptor> deconstructorMethods = new TreeSet<>();
		protected Set<NameDescriptor> deconstructorFields = new TreeSet<>();

		private AnalyzerClassVisitor(int api) {
			super(api);
		}

		public boolean isAnyConstantRelatedSettings() {
			return enumType || constantExpression || !constantExpressionMethods.isEmpty()
					|| !constantExpressionFields.isEmpty() || !deconstructorMethods.isEmpty()
					|| !deconstructorFields.isEmpty();
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			classInternalName = name;
			enumType = ((access & Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM);
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			if (CONSTANTEXPRESSION_DESCRIPTOR.equals(descriptor)) {
				constantExpression = true;
			}
			return super.visitAnnotation(descriptor, visible);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
				@Override
				public AnnotationVisitor visitAnnotation(String annotdescriptor, boolean visible) {
					if (CONSTANTEXPRESSION_DESCRIPTOR.equals(annotdescriptor)) {
						constantExpressionMethods.add(new NameDescriptor(name, descriptor));
					}
					if (DECONSTRUCTOR_DESCRIPTOR.equals(annotdescriptor)) {
						deconstructorMethods.add(new NameDescriptor(name, descriptor));
					}
					return super.visitAnnotation(annotdescriptor, visible);
				}
			};
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			return new FieldVisitor(api, super.visitField(access, name, descriptor, signature, value)) {
				@Override
				public AnnotationVisitor visitAnnotation(String annotdescriptor, boolean visible) {
					if (CONSTANTEXPRESSION_DESCRIPTOR.equals(annotdescriptor)) {
						constantExpressionFields.add(new NameDescriptor(name, descriptor));
					}
					if (DECONSTRUCTOR_DESCRIPTOR.equals(annotdescriptor)) {
						deconstructorFields.add(new NameDescriptor(name, descriptor));
					}
					return super.visitAnnotation(annotdescriptor, visible);
				}
			};
		}
	}
}
