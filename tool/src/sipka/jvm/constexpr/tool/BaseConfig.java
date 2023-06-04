package sipka.jvm.constexpr.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import sipka.jvm.constexpr.tool.options.DeconstructionContext;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.MemberReference;
import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * Contains the default configurations for the inliner tool.
 */
//this class is separate from ConstantExpressionInliner because if this code were in clinit, then that could run
//slower, because the static initializer may be slower to run 
class BaseConfig {
	private static final String CONFIG_TYPE_CONSTANT_TYPE = "CT";
	private static final String CONFIG_TYPE_DECONSTRUCTOR = "DEC";
	private static final String CONFIG_TYPE_ENUM_TYPE = "EN";
	private static final String CONFIG_TYPE_RECONSTRUCTOR = "REC";
	private static final Pattern PATTERN_WHITESPACE = Pattern.compile("[ \\t]+");

	public static void loadBaseConfig(Map<String, InlinerTypeReference> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors,
			Map<String, ConstantDeconstructor> baseConstantDeconstructors) {

		ClassLoader resourceclassloader = BaseConfig.class.getClassLoader();
		ClassLoader loadclassloader = BaseConfig.class.getClassLoader();
		final String filename = "res/base_config";
		try (InputStream in = resourceclassloader.getResourceAsStream(filename)) {
			if (in == null) {
				throw new NoSuchFileException(filename, null,
						"jvm-constexpr ClassLoader resource not found: " + filename);
			}
			Map<String, DeconstructionSelector> deconstructors = new TreeMap<>();

			loadConfigStream(in, loadclassloader, baseConstantTypes, baseConstantReconstructors, deconstructors);

			for (Entry<String, DeconstructionSelector> entry : deconstructors.entrySet()) {
				baseConstantDeconstructors.put(entry.getKey(),
						new ConfigSelectorConstantDeconstructor(entry.getValue()));
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize " + BaseConfig.class.getSimpleName() + " unable to read "
					+ filename + " config file from classpath.", e);
		}

		initReconstructors(baseConstantReconstructors);
		initDeconstructors(baseConstantDeconstructors);
	}

	private static void loadConfigStream(InputStream in, ClassLoader loadclassloader,
			Map<String, InlinerTypeReference> baseConstantTypes,
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors,
			Map<String, DeconstructionSelector> deconstructorSelectors) throws IOException {
		Map<String, DeconstructorConfigs> typedeconsconfigs = new TreeMap<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			reader_loop:
			for (String line; (line = reader.readLine()) != null;) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				String[] split = PATTERN_WHITESPACE.split(line);
				switch (split[0]) {
					case CONFIG_TYPE_CONSTANT_TYPE: {
						String classinternalname = split[1];

						String classname = Type.getObjectType(classinternalname).getClassName();
						try {
							Class<?> type = Class.forName(classname, false, loadclassloader);
							baseConstantTypes.put(classinternalname, new InlinerTypeReference(type));
						} catch (ClassNotFoundException e) {
							baseConstantTypes.put(classinternalname,
									new MemberNotAvailableInlinerTypeReference(classinternalname, e));
						}
						break;
					}
					case CONFIG_TYPE_ENUM_TYPE: {
						String classinternalname = split[1];

						Type asmtype = Type.getObjectType(classinternalname);
						String classname = asmtype.getClassName();
						MethodKey valueofmethodkey = new MethodKey(classinternalname, "valueOf",
								Type.getMethodDescriptor(asmtype, Type.getType(String.class)));

						try {
							Class<?> type = Class.forName(classname, false, loadclassloader);

							addConstantReconstructor(baseConstantReconstructors, valueofmethodkey, loadclassloader);
							baseConstantTypes.put(classinternalname, new InlinerTypeReference(type));
						} catch (ClassNotFoundException e) {
							baseConstantTypes.put(classinternalname,
									new MemberNotAvailableInlinerTypeReference(classinternalname, e));
							baseConstantReconstructors.put(valueofmethodkey, new TypeReferencedConstantReconstructor(
									new MemberNotAvailableConstantReconstructor(valueofmethodkey, e)));
						}
						break;
					}
					case CONFIG_TYPE_RECONSTRUCTOR: {
						String classinternalname = split[1];
						String membername = split[2];
						String descriptor = split[3];

						MemberKey memberkey = MemberKey.create(classinternalname, membername, descriptor);
						addConstantReconstructor(baseConstantReconstructors, memberkey, loadclassloader);
						break;
					}
					case CONFIG_TYPE_DECONSTRUCTOR: {
						String ownerclassinternalname = split[1];

						String membername = split[2];
						if (membername.startsWith("SELECTOR:")) {
							DeconstructorConfigs deconsconfig = typedeconsconfigs
									.computeIfAbsent(ownerclassinternalname, x -> new DeconstructorConfigs());

							//use the specified class as a DeconstructionSelector
							String selectorclass = membername.substring("SELECTOR:".length());
							try {
								Constructor<? extends DeconstructionSelector> constructor = Class
										.forName(selectorclass, false, loadclassloader)
										.asSubclass(DeconstructionSelector.class).getConstructor();
								constructor.setAccessible(true);
								DeconstructionSelector selector = constructor.newInstance();
								deconsconfig.selectors.add(selector);

							} catch (Exception e) {
								deconsconfig.classNotAvailable(selectorclass, e);
							}
							continue reader_loop;
						}
						String descriptor = split[3];

						Type membertype = Type.getType(descriptor);
						Type ownerasmtype = Type.getObjectType(ownerclassinternalname);
						if (membertype.getSort() == Type.METHOD) {
							Type deconstructedasmtype;
							boolean constructormethod = Utils.CONSTRUCTOR_METHOD_NAME.equals(membername);
							if (constructormethod) {
								deconstructedasmtype = ownerasmtype;
							} else {
								deconstructedasmtype = membertype.getReturnType();
							}

							String deconstructedasmtypeinternalname = deconstructedasmtype.getInternalName();

							DeconstructorConfigs deconsconfig = typedeconsconfigs
									.computeIfAbsent(deconstructedasmtypeinternalname, x -> new DeconstructorConfigs());

							Class<?> deconstructedtype;
							try {
								deconstructedtype = Class.forName(deconstructedasmtype.getClassName(), false,
										loadclassloader);
							} catch (ClassNotFoundException e) {

								deconsconfig.classNotAvailable(deconstructedasmtypeinternalname, e);
								continue reader_loop;
							}

							Type[] argumenttypes = membertype.getArgumentTypes();
							DeconstructionDataAccessor[] fieldaccessors = new DeconstructionDataAccessor[argumenttypes.length];
							int lastidx = 4;
							for (int i = 0; i < fieldaccessors.length; i++) {
								String getter = split[lastidx++];
								int parenidx = getter.indexOf('(');
								if (parenidx < 0) {
									throw new IllegalArgumentException(
											"Unrecognized data accessor format: " + getter + " in " + line);
								}
								//only method accessors supported for now
								String gettername = getter.substring(0, parenidx);
								String gettermethoddesc = getter.substring(parenidx);

								Method method;
								try {
									method = Utils.getMethodForMethodDescriptor(deconstructedtype, null,
											gettermethoddesc, gettername);
								} catch (NoSuchMethodException e) {
									deconsconfig.memberNotAvailable(deconstructedasmtypeinternalname, gettername,
											gettermethoddesc, e);
									continue reader_loop;
								}
								fieldaccessors[i] = DeconstructionDataAccessor.createForMethodWithReceiver(method,
										argumenttypes[i]);
							}

							Executable executable;

							if (constructormethod) {
								try {
									executable = Utils.getConstructorForMethodDescriptor(deconstructedtype, descriptor);
								} catch (NoSuchMethodException e) {
									deconsconfig.memberNotAvailable(deconstructedasmtypeinternalname, membername,
											descriptor, e);
									continue reader_loop;
								}
							} else {
								Class<?> methodownertype;
								if (ownerasmtype.equals(deconstructedasmtype)) {
									methodownertype = deconstructedtype;
								} else {
									try {
										methodownertype = Class.forName(ownerasmtype.getClassName(), false,
												loadclassloader);
									} catch (ClassNotFoundException e) {
										deconsconfig.classNotAvailable(ownerclassinternalname, e);
										continue reader_loop;
									}
								}
								try {
									executable = Utils.getMethodForMethodDescriptor(methodownertype,
											ownerclassinternalname, descriptor, membername);
								} catch (NoSuchMethodException e) {
									deconsconfig.memberNotAvailable(ownerclassinternalname, membername, descriptor, e);
									continue reader_loop;
								}
							}

							deconsconfig.selectors.add(DeconstructionSelector.getForConfiguration(
									DeconstructorConfiguration.createExecutable(executable, fieldaccessors)));
						} else {
							//it is a field
							if (!membertype.equals(ownerasmtype)) {
								//only support fields that are declared in the same type for now
								throw new UnsupportedOperationException("unsupported deconstructor type: " + line);
							}

							DeconstructorConfigs deconsconfig = typedeconsconfigs
									.computeIfAbsent(membertype.getInternalName(), x -> new DeconstructorConfigs());

							Class<?> ownertype;
							try {
								ownertype = Class.forName(ownerasmtype.getClassName(), false, loadclassloader);
							} catch (ClassNotFoundException e) {

								deconsconfig.classNotAvailable(ownerclassinternalname, e);
								continue reader_loop;
							}

							try {
								Field field = Utils.getFieldForDescriptor(ownertype, membername, descriptor);
								deconsconfig.selectors.add(DeconstructionSelector.getStaticFieldEquality(field));
							} catch (NoSuchFieldException e) {
								deconsconfig.memberNotAvailable(ownerclassinternalname, membername, descriptor, e);
								continue reader_loop;
							}
						}
						break;
					}
					default: {
						throw new IllegalArgumentException("Unrecognized config type: " + line);
					}
				}
			}
		}

		for (Entry<String, DeconstructorConfigs> entry : typedeconsconfigs.entrySet()) {
			DeconstructionSelector selector = entry.getValue().toDeconstructionSelector();
			if (selector == null) {
				continue;
			}
			deconstructorSelectors.put(entry.getKey(), selector);
		}
	}

	private static void initReconstructors(
			Map<MemberKey, TypeReferencedConstantReconstructor> baseConstantReconstructors) {
		//specifies ways of creating instances of types from the stack data

		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassNameConstantReconstructor.INSTANCE, Class.class));
		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getSimpleName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassSimpleNameConstantReconstructor.INSTANCE, Class.class));
		baseConstantReconstructors.put(new MethodKey("java/lang/Class", "getCanonicalName", "()Ljava/lang/String;"),
				new TypeReferencedConstantReconstructor(ClassCanonicalNameConstantReconstructor.INSTANCE, Class.class));

		//blacklist this method, as it depends on locale, and resources, so on the executing environment
		baseConstantReconstructors.putIfAbsent(
				new MethodKey(Type.getInternalName(ChronoField.class), "getDisplayName",
						Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Locale.class))),
				new TypeReferencedConstantReconstructor(NotReconstructableConstantReconstructor.INSTANCE,
						ChronoField.class));
	}

	private static void initDeconstructors(Map<String, ConstantDeconstructor> baseConstantDeconstructors) {
		//specifies ways of writing the type instances to the stack
		setDeconstructor(baseConstantDeconstructors, String.class, StringConstantDeconstructor.INSTANCE);
		setDeconstructor(baseConstantDeconstructors, StringBuilder.class, StringBuilderConstantDeconstructor.INSTANCE);
	}

	private static void setDeconstructor(Map<String, ConstantDeconstructor> constantDeconstructors, Class<?> type,
			ConstantDeconstructor deconstructor) {
		Object prev = constantDeconstructors.putIfAbsent(Type.getInternalName(type), deconstructor);
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant deconstructor for: " + type);
		}
	}

	private static void addConstantReconstructor(
			Map<? super MemberKey, ? super TypeReferencedConstantReconstructor> reconstructors, MemberKey memberkey,
			ClassLoader loadclassloader) {
		ConstantReconstructor reconstructor;
		Class<?> type = null;
		if (memberkey instanceof MethodKey) {
			MethodKey methodkey = (MethodKey) memberkey;
			try {
				type = Class.forName(Type.getObjectType(memberkey.getOwner()).getClassName(), false, loadclassloader);
				Executable executable = Utils.getExecutableForDescriptor(type, methodkey.getOwner(),
						methodkey.getMemberName(), methodkey.getMethodDescriptor());
				if (executable instanceof Method) {
					reconstructor = new MethodBasedConstantReconstructor((Method) executable,
							Modifier.isStatic(executable.getModifiers()) ? ReconstructorPredicate.ALLOW_ALL
									: ReconstructorPredicate.ALLOW_INSTANCE_OF);
				} else if (executable instanceof Constructor<?>) {
					reconstructor = new ConstructorBasedConstantReconstructor((Constructor<?>) executable);
				} else {
					throw new IllegalArgumentException("Unknown executable type: " + executable + " for " + memberkey);
				}

			} catch (ReflectiveOperationException e) {
				reconstructor = new MemberNotAvailableConstantReconstructor(methodkey, e);
			}
		} else if (memberkey instanceof FieldKey) {
			FieldKey fieldkey = (FieldKey) memberkey;
			try {
				type = Class.forName(Type.getObjectType(memberkey.getOwner()).getClassName(), false, loadclassloader);
				Field f = Utils.getFieldForDescriptor(type, fieldkey.getMemberName(), fieldkey.getFieldDescriptor());
				reconstructor = new FieldBasedConstantReconstructor(f);
			} catch (ReflectiveOperationException e) {
				reconstructor = new MemberNotAvailableConstantReconstructor(fieldkey, e);
			}
		} else {
			throw new IllegalArgumentException(
					"Unknown " + MemberKey.class.getSimpleName() + " subclass: " + memberkey.getClass());
		}
		Object prev = reconstructors.putIfAbsent(memberkey,
				new TypeReferencedConstantReconstructor(reconstructor, type));
		if (prev != null) {
			throw new IllegalArgumentException("Duplicate constant reconstructor for: " + memberkey);
		}
	}

	static final class MemberNotAvailableConstantDeconstructor
			implements ConstantDeconstructor, DeconstructionSelector {
		private final String classInternalName;
		private final String memberName;
		private final String memberDescriptor;
		private final Exception exception;

		MemberNotAvailableConstantDeconstructor(MemberKey memberkey, Exception e) {
			this.classInternalName = memberkey.getOwner();
			this.memberName = memberkey.getMemberName();
			this.memberDescriptor = MemberKey.getDescriptor(memberkey);
			this.exception = e;
		}

		MemberNotAvailableConstantDeconstructor(String classinternalname, String membername, String memberdescr,
				Exception e) {
			this.classInternalName = classinternalname;
			this.memberName = membername;
			this.memberDescriptor = memberdescr;
			this.exception = e;
		}

		MemberNotAvailableConstantDeconstructor(String classinternalname, Exception e) {
			this(classinternalname, null, null, e);
		}

		public String getClassInternalName() {
			return classInternalName;
		}

		public String getMemberName() {
			return memberName;
		}

		public String getMemberDescriptor() {
			return memberDescriptor;
		}

		public Exception getException() {
			return exception;
		}

		@Override
		public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
				MethodNode methodnode, Object value) {
			//only for logging.
			context.logConfigClassMemberInaccessible(classInternalName, memberName, memberDescriptor, exception);
			return null;
		}

		@Override
		public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
				Object value) {
			//only for logging.
			deconstructioncontext.logConfigClassMemberInaccessible(classInternalName, memberName, memberDescriptor,
					exception);
			return null;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(getClass().getSimpleName());
			builder.append("[memberName=");
			builder.append(memberName);
			builder.append(", memberDescriptor=");
			builder.append(memberDescriptor);
			builder.append(", classInternalName=");
			builder.append(classInternalName);
			builder.append(", exception=");
			builder.append(exception);
			builder.append("]");
			return builder.toString();
		}
	}

	private static final class MemberNotAvailableInlinerTypeReference extends InlinerTypeReference {
		private final String memberName;
		private final String memberDescr;
		private final String classInternalName;
		private final ReflectiveOperationException e;

		public MemberNotAvailableInlinerTypeReference(String classInternalName, String memberName, String memberDescr,
				ReflectiveOperationException e) {
			super(null);
			this.memberName = memberName;
			this.memberDescr = memberDescr;
			this.classInternalName = classInternalName;
			this.e = e;
		}

		public MemberNotAvailableInlinerTypeReference(String classInternalName, ClassNotFoundException e) {
			this(classInternalName, null, null, e);
		}

		@Override
		public Class<?> getType(ConstantExpressionInliner context) {
			context.logConfigClassMemberInaccessible(classInternalName, memberName, memberDescr, e);
			return null;
		}
	}

	private static final class MemberNotAvailableConstantReconstructor implements ConstantReconstructor {
		private final String classInternalName;
		private final String memberName;
		private final String memberDescriptor;
		private final ReflectiveOperationException exception;
		private final ConstantReconstructor delegate;

		public MemberNotAvailableConstantReconstructor(MemberKey memberkey, ReflectiveOperationException exception) {
			this(memberkey, exception, null);
		}

		public MemberNotAvailableConstantReconstructor(MemberKey memberkey, ReflectiveOperationException exception,
				ConstantReconstructor delegate) {
			this.classInternalName = memberkey.getOwner();
			this.memberName = memberkey.getMemberName();
			this.memberDescriptor = MemberKey.getDescriptor(memberkey);
			this.exception = exception;
			this.delegate = delegate;
		}

		@Override
		public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
				throws ReconstructionException {
			context.getInliner().logConfigClassMemberInaccessible(classInternalName, memberName, memberDescriptor,
					exception);
			return delegate == null ? null : delegate.reconstructValue(context, ins);
		}

	}

	private static final class ClassCanonicalNameConstantReconstructor extends ClassMethodConstantReconstructor {
		public static final ClassCanonicalNameConstantReconstructor INSTANCE = new ClassCanonicalNameConstantReconstructor(
				"getCanonicalName");

		public ClassCanonicalNameConstantReconstructor(String memberName) {
			super(memberName);
		}

		@Override
		protected String getValue(ReconstructionContext context, Type t) {
			return Utils.getCanonicalNameOfClass(t);
		}

		@Override
		protected String getValue(ReconstructionContext context, Class<?> c) {
			return c.getCanonicalName();
		}
	}

	private static final class ClassSimpleNameConstantReconstructor extends ClassMethodConstantReconstructor {
		public static final ClassSimpleNameConstantReconstructor INSTANCE = new ClassSimpleNameConstantReconstructor(
				"getSimpleName");

		public ClassSimpleNameConstantReconstructor(String memberName) {
			super(memberName);
		}

		@Override
		protected String getValue(ReconstructionContext context, Type t) {
			return Utils.getSimpleNameOfClass(t);
		}

		@Override
		protected String getValue(ReconstructionContext context, Class<?> c) {
			return c.getSimpleName();
		}
	}

	private static final class ClassNameConstantReconstructor extends ClassMethodConstantReconstructor {
		public static final ClassNameConstantReconstructor INSTANCE = new ClassNameConstantReconstructor("getName");

		public ClassNameConstantReconstructor(String memberName) {
			super(memberName);
		}

		@Override
		protected String getValue(ReconstructionContext context, Type t) {
			return Utils.getNameOfClass(t);
		}

		@Override
		protected String getValue(ReconstructionContext context, Class<?> c) {
			return c.getName();
		}
	}

	private abstract static class ClassMethodConstantReconstructor implements ConstantReconstructor {
		private final String memberName;

		public ClassMethodConstantReconstructor(String memberName) {
			this.memberName = memberName;
		}

		@Override
		public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
				throws ReconstructionException {
			AsmStackReconstructedValue typeval;
			MethodInsnNode mins = (MethodInsnNode) ins;
			try {
				AbstractInsnNode previns = ins.getPrevious();
				//if the previous one is an LDC instruction, then get the Type constant directly
				//instead of reconstructing the Class instance, as that is not really necessary
				if (previns.getOpcode() == Opcodes.LDC) {
					LdcInsnNode ldcins = (LdcInsnNode) previns;
					typeval = AsmStackReconstructedValue.createConstant(previns, ins, ldcins.cst);
				} else {
					typeval = context.getInliner().reconstructStackValue(context.withReceiverType(Class.class),
							previns);
				}
			} catch (ReconstructionException e) {
				throw context.newInstanceAccessFailureReconstructionException(e, ins, mins.owner, mins.name, mins.desc);
			}
			if (typeval == null) {
				return null;
			}
			Object val = typeval.getValue();
			if (val == null) {
				return null;
			}
			Object result;
			if (val instanceof Type) {
				result = getValue(context, (Type) val);
			} else if (val instanceof Class) {
				result = getValue(context, (Class<?>) val);
			} else {
				//some unrecognized type for Class method call
				//can't do much, maybe programming error, or something?
				return null;
			}
			return new AsmStackReconstructedValue(typeval.getFirstIns(), ins.getNext(),
					AsmStackInfo.createMethod(Type.getType(Class.class), mins.name, Type.getMethodType(mins.desc),
							typeval.getStackInfo(), AsmStackInfo.EMPTY_ASMSTACKINFO_ARRAY),
					result);
		}

		protected abstract Object getValue(ReconstructionContext context, Type t);

		protected abstract Object getValue(ReconstructionContext context, Class<?> c);

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + memberName + "]";
		}

	}

	private static final class DeconstructorConfigs {
		protected final Map<FieldKey, Field> fields = new TreeMap<>(MemberKey::compare);
		protected final Map<MemberReference, MemberNotAvailableConstantDeconstructor> notAvailableDeconstructors = new TreeMap<>();
		protected final Set<DeconstructionSelector> selectors = new LinkedHashSet<>();

		public DeconstructionSelector toDeconstructionSelector() {
			List<DeconstructionSelector> selectors = new ArrayList<>();
			selectors.addAll(notAvailableDeconstructors.values());
			if (!fields.isEmpty()) {
				selectors.add(DeconstructionSelector.getStaticFieldEquality(fields.values().toArray(new Field[0])));
			}
			selectors.addAll(this.selectors);
			return DeconstructionSelector.getMultiSelector(selectors.toArray(new DeconstructionSelector[0]));
		}

		public void classNotAvailable(String classinternamname, Exception exc) {
			this.notAvailableDeconstructors.put(new MemberReference(classinternamname, "", ""),
					new MemberNotAvailableConstantDeconstructor(classinternamname, exc));
		}

		public void memberNotAvailable(String classinternamname, String membername, String memberdescriptor,
				Exception exc) {
			this.notAvailableDeconstructors.put(new MemberReference(classinternamname, membername, memberdescriptor),
					new MemberNotAvailableConstantDeconstructor(classinternamname, membername, memberdescriptor, exc));
		}
	}
}
