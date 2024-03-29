package testing.sipka.jvm.constexpr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderDataFinder;
import saker.build.thirdparty.saker.util.classloader.MultiDataClassLoader;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.StreamUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.annotations.Deconstructor;
import sipka.jvm.constexpr.tool.ConstantExpressionInliner;
import sipka.jvm.constexpr.tool.OutputConsumer;
import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.AnnotationVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassWriter;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.FieldVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.MethodVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.RecordComponentVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.TypePath;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;
import testing.saker.SakerTestCase;

public class TestUtils {

	private static final class MapOutputConsumer implements OutputConsumer {
		private final NavigableMap<String, byte[]> outputs = new TreeMap<>();

		@Override
		public void put(ToolInput<?> input, byte[] resultBytes) throws IOException {
			String inputkey = (String) input.getInputKey();
			byte[] prev = outputs.putIfAbsent(inputkey, resultBytes);
			if (prev != null) {
				throw new IllegalArgumentException("Duplicate output: " + inputkey);
			}
		}

		public NavigableMap<String, byte[]> getOutputs() {
			return outputs;
		}
	}

	private static Collection<ToolInput<?>> createInputsForClasses(Class<?>... classes) {
		ArrayList<ToolInput<?>> result = new ArrayList<>();
		for (Class<?> c : classes) {
			ByteArrayRegion bytes = ReflectUtils.getClassBytesUsingClassLoader(c);
			String path = c.getName().replace('.', '/') + ".class";
			result.add(new TestToolInput(path, bytes.copyOptionally()));
		}
		return result;
	}

	public static NavigableMap<String, byte[]> performInlining(Class<?>... classes) throws IOException {
		InlinerOptions opts = createOptionsForClasses(classes);
		return performInlining(opts);
	}

	public static NavigableMap<String, byte[]> performInlining(InlinerOptions opts) throws IOException {
		MapOutputConsumer outconsumer = new MapOutputConsumer();
		opts.setOutputConsumer(outconsumer);
		ConstantExpressionInliner.run(opts);
		return outconsumer.getOutputs();
	}

	public static InlinerOptions createOptionsForClasses(Class<?>... classes) {
		InlinerOptions opts = new InlinerOptions();
		opts.setLogger(new TestCollectingLogger());
		opts.setClassLoader(TestUtils.class.getClassLoader()); // init with the testing classloader
		opts.setInputs(createInputsForClasses(classes));

		return opts;
	}

	public static NavigableMap<String, ClassNode> performInliningClassNodes(Class<?>... classes) throws IOException {
		NavigableMap<String, byte[]> outbytes = performInlining(classes);
		return bytesToClassNodes(outbytes);
	}

	public static NavigableMap<String, ClassNode> performInliningClassNodes(InlinerOptions opts) throws IOException {
		NavigableMap<String, byte[]> outbytes = performInlining(opts);
		return bytesToClassNodes(outbytes);
	}

	public static NavigableMap<String, ClassNode> bytesToClassNodes(NavigableMap<String, byte[]> classbytes) {
		NavigableMap<String, ClassNode> outputs = new TreeMap<>();
		for (Entry<String, byte[]> entry : classbytes.entrySet()) {
			ClassReader cr = new ClassReader(entry.getValue());
			ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
			outputs.put(entry.getKey(), cn);
		}
		return outputs;
	}

	public static Map<String, ClassNode> filesToClassNodes(Iterable<? extends Path> files) throws IOException {
		NavigableMap<String, ClassNode> result = new TreeMap<>();
		for (Path path : files) {
			ClassReader cr = new ClassReader(Files.readAllBytes(path));
			ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
			cr.accept(cn, ClassReader.EXPAND_FRAMES);
			result.put(Type.getObjectType(cn.name).getClassName(), cn);
		}
		return result;
	}

	public static Map<String, ClassNode> jarToClassNodes(Path jarfile) throws IOException {
		NavigableMap<String, ClassNode> result = new TreeMap<>();
		try (InputStream is = Files.newInputStream(jarfile);
				JarInputStream jis = new JarInputStream(is)) {
			for (ZipEntry ze; (ze = jis.getNextEntry()) != null;) {
				if (!ze.getName().endsWith(".class")) {
					continue;
				}
				ClassReader cr = new ClassReader(jis);
				ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
				cr.accept(cn, ClassReader.EXPAND_FRAMES);
				result.put(Type.getObjectType(cn.name).getClassName(), cn);
			}
		}
		return result;
	}

	public static Map<String, ClassNode> directoryToClassNodes(Path dir) throws IOException {
		NavigableMap<String, ClassNode> result = new TreeMap<>();
		try (Stream<Path> walkstream = Files.walk(dir)) {
			for (Iterator<Path> it = walkstream.iterator(); it.hasNext();) {
				Path p = it.next();
				if (Files.isDirectory(p)) {
					continue;
				}

				if (!p.getFileName().toString().endsWith(".class")) {
					continue;
				}
				byte[] bytes = Files.readAllBytes(p);
				ClassReader cr = new ClassReader(bytes);
				ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
				cr.accept(cn, ClassReader.EXPAND_FRAMES);
				result.put(Type.getObjectType(cn.name).getClassName(), cn);
			}

		}
		return result;
	}

	public static NavigableMap<String, FieldNode> getFields(ClassNode cn) {
		TreeMap<String, FieldNode> result = new TreeMap<>();
		for (FieldNode fn : cn.fields) {
			result.put(fn.name, fn);
		}
		return result;
	}

	public static Object readField(Object o, String name)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Class<? extends Object> c = o.getClass();
		Field f = c.getField(name);
		return f.get(o);
	}

	public static Object readFieldOfStaticField(Class<?> type, String staticfieldname, String fieldname)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		return readField(type.getField(staticfieldname).get(null), fieldname);
	}

	public static void assertSameStaticFieldValues(ClassNode cn, Class<?> type, String... skipasmfields)
			throws Exception {
		Class<?> loadedclass = loadClass(cn);

		assertSameStaticFieldValues(cn, type, loadedclass, skipasmfields);
	}

	public static void assertSameStaticFieldValues(ClassNode cn, Class<?> type, Class<?> loadedclass,
			String... skipasmfields) throws AssertionError, IllegalAccessException, NoSuchFieldException {
		String typename = type.getName();
		Set<String> asmfieldstoskip = new TreeSet<>(Arrays.asList(skipasmfields));

		SakerTestCase.assertNotIdentityEquals(loadedclass, type);

		NavigableMap<String, FieldNode> fields = TestUtils.getFields(cn);
		for (Field f : type.getDeclaredFields()) {
			if (((f.getModifiers() & Modifier.STATIC) != Modifier.STATIC)) {
				continue;
			}
			String fieldname = f.getName();

			FieldNode fnode = fields.get(fieldname);
			Class<?> fieldtype = f.getType();
			Object asmval = TestUtils.getAsmFieldNodeValue(fnode, fieldtype);

			Object fieldval = f.get(null);

			Object loadedval = loadedclass.getField(fieldname).get(null);

			System.out.println(typename + "." + fieldname + ": " + asmval + " - " + fieldval + " (0x"
					+ Integer.toHexString(System.identityHashCode(fieldval)) + ") - " + loadedval + " (0x"
					+ Integer.toHexString(System.identityHashCode(loadedval)) + ")");

			if (fieldname.startsWith("random_")) {
				//only check nullability
				SakerTestCase.assertNonNull(loadedval, "LOADED null");
				SakerTestCase.assertNonNull(fieldval, "CLASS null");
			} else {
				if (ObjectUtils.isSameClass(loadedval, fieldval)) {
					//can't check equality if the field values are from different classloaders
					if (ObjectUtils.classOf(loadedval) == Class.class) {
						//if the values are Class instances, then check the class names instead of equality
						//as they might be from different classloaders
						SakerTestCase.assertEquals(((Class<?>) loadedval).getName(), ((Class<?>) fieldval).getName());
					} else {
						SakerTestCase.assertEquals(loadedval, fieldval, fieldname + ": LOADED - CLASS");
					}
				}
			}

			if (asmfieldstoskip.contains(fieldname)) {
				continue;
			}
			if (!fieldtype.isPrimitive() && !String.class.equals(fieldtype)) {
				//can't test these for equality between the ASM and loaded class, as the field value is not
				//set on the FieldNode
				continue;
			}

			if (fieldname.startsWith("random_")) {
				//only check nullability
				SakerTestCase.assertNonNull(asmval, "ASM null");
				SakerTestCase.assertNonNull(fieldval, "CLASS null");
			} else {
				SakerTestCase.assertEquals(asmval, fieldval, fieldname + ": ASM - CLASS");
			}
		}
	}

	public static Class<?> loadClass(ClassNode cn) throws ClassNotFoundException {
		return loadClass(ClassLoader.getSystemClassLoader(), cn);
	}

	public static Class<?> loadClass(ClassLoader parentcl, ClassNode cn) throws ClassNotFoundException {
		String cname = Type.getObjectType(cn.name).getClassName();

		ClassLoader cl = createClassLoader(parentcl, cn);
		Class<?> loadedclass = Class.forName(cname, true, cl);
		return loadedclass;
	}

	public static ClassLoader createJarClassLoader(Path jar) throws NullPointerException, IOException {
		Map<String, ByteArrayRegion> resourceBytes = new TreeMap<>();
		try (InputStream is = Files.newInputStream(jar);
				JarInputStream jis = new JarInputStream(is)) {
			for (ZipEntry ze; (ze = jis.getNextEntry()) != null;) {
				resourceBytes.put(ze.getName(), StreamUtils.readStreamFully(jis));
			}
		}
		return new MultiDataClassLoader(ClassLoader.getSystemClassLoader(),
				new MemoryClassLoaderDataFinder(resourceBytes));
	}

	public static ClassNode loadClassNodeFromJar(Path jar, String classname) throws IOException {
		String searchname = classname.replace('.', '/') + ".class";
		try (InputStream is = Files.newInputStream(jar);
				JarInputStream jis = new JarInputStream(is)) {
			for (ZipEntry ze; (ze = jis.getNextEntry()) != null;) {
				if (ze.getName().equals(searchname)) {
					return loadClassFromStream(jis);
				}
			}
		}
		throw new NoSuchFileException(searchname);
	}

	private static ClassNode loadClassFromStream(InputStream jis) throws IOException {
		ClassReader cr = new ClassReader(jis);
		ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
		cr.accept(cn, ClassReader.EXPAND_FRAMES);
		return cn;
	}

	public static ClassLoader createClassLoader(Object... nodesorclasses) {
		return createClassLoader(ClassLoader.getSystemClassLoader(), nodesorclasses);
	}

	public static ClassLoader createClassLoader(ClassLoader parentcl, Object... nodesorclasses) {
		Map<String, ByteArrayRegion> clresources = new TreeMap<>();
		for (Object o : nodesorclasses) {
			ByteArrayRegion bytes;
			String cname;
			if (o instanceof Class<?>) {
				Class<?> c = (Class<?>) o;
				cname = c.getName();
				bytes = ReflectUtils.getClassBytesUsingClassLoader(c);
			} else {
				ClassNode cn = (ClassNode) o;
				cname = Type.getObjectType(cn.name).getClassName();
				bytes = ByteArrayRegion.wrap(toClassBytes(cn));
			}
			SakerTestCase.assertNonNull(bytes, cname);
			clresources.put(cname.replace('.', '/') + ".class", bytes);
		}
		return new MultiDataClassLoader(parentcl, new MemoryClassLoaderDataFinder(clresources));
	}

	public static byte[] toClassBytes(ClassNode cn) {
		ClassWriter cw = new ClassWriter(0);
		cn.accept(cw);
		return cw.toByteArray();
	}

	public static MethodNode getClInitMethod(ClassNode cn) {
		for (MethodNode mn : cn.methods) {
			if (!"<clinit>".equals(mn.name)) {
				continue;
			}
			return mn;
		}
		return null;
	}

	public static void assertNoPutStaticInClInit(ClassNode cn) {
		MethodNode mn = getClInitMethod(cn);
		if (mn != null) {
			assertNoOpcodeInMethod(mn, Opcodes.PUTSTATIC);
		}
	}

	public static void assertNoInvokeStaticInClInit(ClassNode cn) {
		MethodNode mn = getClInitMethod(cn);
		if (mn != null) {
			assertNoOpcodeInMethod(mn, Opcodes.INVOKESTATIC);
		}
	}

	public static void assertNoInvokeSpecialInClInit(ClassNode cn) {
		MethodNode mn = getClInitMethod(cn);
		if (mn != null) {
			assertNoOpcodeInMethod(mn, Opcodes.INVOKESPECIAL);
		}
	}

	public static void assertNoOpcodeInMethod(MethodNode mn, int checkopcode) throws AssertionError {
		for (AbstractInsnNode ins : mn.instructions) {
			if (ins.getOpcode() == checkopcode) {
				throw new AssertionError(checkopcode + " opcode in " + mn.name + mn.desc + " with " + ins);
			}
		}
	}

	public static MethodNode getMethodNode(ClassNode cn, String name, String descriptor) {
		for (MethodNode mn : cn.methods) {
			if (name.equals(mn.name) && descriptor.equals(mn.desc)) {
				return mn;
			}
		}
		return null;
	}

	public static boolean isContainsInvokeStatic(MethodNode mn, Class<?> c, String name, Class<?>... paramtypes) {
		return isContainsInvokeOpcode(mn, Opcodes.INVOKESTATIC, c, name, paramtypes);
	}

	public static boolean isContainsInvokeVirtual(MethodNode mn, Class<?> c, String name, Class<?>... paramtypes) {
		return isContainsInvokeOpcode(mn, Opcodes.INVOKEVIRTUAL, c, name, paramtypes);
	}

	public static boolean isContainsInvokeConstructor(MethodNode mn, Class<?> c, Class<?>... paramtypes) {
		return isContainsInvokeOpcode(mn, Opcodes.INVOKESPECIAL, c, Utils.CONSTRUCTOR_METHOD_NAME, paramtypes);
	}

	public static boolean isContainsInvokeOpcode(MethodNode mn, int opcode, Class<?> c, String name,
			Class<?>... paramtypes) {
		for (AbstractInsnNode ins : mn.instructions) {
			if (ins.getOpcode() != opcode) {
				continue;
			}
			MethodInsnNode mins = (MethodInsnNode) ins;
			if (!mins.name.equals(name)) {
				continue;
			}
			if (!Type.getInternalName(c).equals(mins.owner)) {
				continue;
			}
			if (!Utils.isSameTypes(paramtypes, Type.getArgumentTypes(mins.desc))) {
				continue;
			}
			return true;
		}
		return false;
	}

	public static Object getAsmFieldNodeValue(FieldNode fnode, Class<?> type) {
		Object asmval = fnode.value;
		if (asmval == null) {
			return null;
		}
		if (type == byte.class) {
			asmval = ((Number) asmval).byteValue();
		} else if (type == short.class) {
			asmval = ((Number) asmval).shortValue();
		} else if (type == boolean.class) {
			asmval = ((Number) asmval).intValue() != 0;
		} else if (type == char.class) {
			asmval = (char) ((Number) asmval).intValue();
		}
		return asmval;
	}

	public static Field[] getFields(Class<?> type, String... fields) {
		Field[] result = new Field[fields.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = ReflectUtils.getFieldAssert(type, fields[i]);
		}
		return result;
	}

	public static Map<Class<?>, Path> writeClasses(Path path, Class<?>... classes) throws IOException {
		Map<Class<?>, Path> result = new LinkedHashMap<>();
		for (Class<?> c : classes) {
			Path classfilepath = path.resolve(c.getName().replace('.', '/') + ".class");
			Files.createDirectories(classfilepath.getParent());
			writeFileIfChanged(classfilepath, ReflectUtils.getClassBytesUsingClassLoader(c).copyOptionally());
			result.put(c, classfilepath);
		}
		return result;
	}

	public static void writeJar(Path path, Class<?>... classes) throws IOException {
		Files.createDirectories(path.getParent());
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ZipOutputStream zos = new ZipOutputStream(baos)) {
				for (int i = 0; i < classes.length; i++) {
					Class<?> c = classes[i];
					ZipEntry ze = new ZipEntry(Type.getInternalName(c) + ".class");
					ze.setTime(0);
					zos.putNextEntry(ze);
					zos.write(ReflectUtils.getClassBytesUsingClassLoader(c).copyOptionally());
					zos.closeEntry();
				}
			}
			writeFileIfChanged(path, baos);
		}
	}

	private static void writeFileIfChanged(Path path, ByteArrayOutputStream baos) throws IOException {
		writeFileIfChanged(path, baos.toByteArray());
	}

	private static void writeFileIfChanged(Path path, byte[] filebytes) throws IOException {
		try {
			//only write if changed, dont need to unnecessarily write the disk for test cases
			if (Arrays.equals(filebytes, Files.readAllBytes(path))) {
				return;
			}
		} catch (Exception e) {
		}
		try {
			Files.write(path, filebytes);
		} catch (Exception e) {
			throw e;
		}
	}

	public static Map<Member, ReconstructorPredicate> allowAllMembers(Collection<? extends Member> members) {
		HashMap<Member, ReconstructorPredicate> result = new HashMap<>();
		for (Member m : members) {
			result.put(m, ReconstructorPredicate.ALLOW_ALL);
		}
		return result;
	}

	public static void assertJarAnnotationsRemoved(Path jarpath) throws IOException {
		try (JarFile jv = new JarFile(jarpath.toFile())) {
			Enumeration<JarEntry> entries = jv.entries();
			while (entries.hasMoreElements()) {
				JarEntry je = entries.nextElement();
				try (InputStream is = jv.getInputStream(je)) {
					assertAnnotationsRemoved(loadClassFromStream(is));
				}
			}
		}
	}

	public static void assertAnnotationsRemoved(ClassNode cn) {
		Set<String> checktypes = ImmutableUtils.makeImmutableNavigableSet(
				new String[] { Type.getDescriptor(ConstantExpression.class), Type.getDescriptor(Deconstructor.class) });
		cn.accept(new AnnotationRemovalCheckerClassVisitor(checktypes, cn));
	}

	public static class MemoryClassLoaderDataFinder implements ClassLoaderDataFinder {
		private Map<String, ByteArrayRegion> resourceBytes;

		public MemoryClassLoaderDataFinder(Map<String, ByteArrayRegion> resourceBytes) {
			this.resourceBytes = resourceBytes;
		}

		@Override
		public Supplier<ByteSource> getResource(String name) {
			ByteArrayRegion got = resourceBytes.get(name);
			if (got == null) {
				return null;
			}
			return () -> new UnsyncByteArrayInputStream(got);
		}
	}

	private static final class AnnotationRemovalCheckerClassVisitor extends ClassVisitor {
		private final Set<String> checkTypes;
		private final ClassNode cn;

		private AnnotationRemovalCheckerClassVisitor(Set<String> checktypes, ClassNode cn) {
			super(ConstantExpressionInliner.ASM_API);
			this.checkTypes = checktypes;
			this.cn = cn;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
			return super.visitAnnotation(descriptor, visible);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
			return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			return new FieldVisitor(api, super.visitField(access, name, descriptor, signature, value)) {
				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitAnnotation(descriptor, visible);
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
				}
			};
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitAnnotation(descriptor, visible);
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
				}
			};
		}

		@Override
		public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
			return new RecordComponentVisitor(api, super.visitRecordComponent(name, descriptor, signature)) {
				@Override
				public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitAnnotation(descriptor, visible);
				}

				@Override
				public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
						boolean visible) {
					SakerTestCase.assertFalse(checkTypes.contains(descriptor), () -> descriptor + " in " + cn.name);
					return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
				}
			};
		}
	}
}
