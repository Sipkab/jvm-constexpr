package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

/**
 * Tests that the CLI main can be run on mixed type of inputs, and that they are properly overwritten.
 */
@SakerTest
public class MixInputTypeOverwriteCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path testbasedir = testCaseDirectory;
		Path classpathdirpath = testbasedir.resolve("classpath");
		TestUtils.writeClasses(classpathdirpath, ConstantClassPath.class);

		Path jarpath = testbasedir.resolve("classes.jar");
		TestUtils.writeJar(jarpath, ConstantsJar.class);
		Path classdirpath = testbasedir.resolve("classdir");
		Map<Class<?>, Path> cdirfiles = TestUtils.writeClasses(classdirpath, ConstantsClassDir.class);
		Map<Class<?>, Path> directfiles = TestUtils.writeClasses(testbasedir.resolve("classes"),
				ConstantsClassFile.class);

		List<String> args = new ArrayList<>();

		for (Path path : directfiles.values()) {
			args.add("-input");
			args.add(path.toString());
		}
		args.add("-input");
		args.add(jarpath.toString());
		args.add("-input");
		args.add(classdirpath.toString());
		args.add("-classpath");
		args.add(classpathdirpath.toString());
		args.add("-overwrite");

		System.out.println("Args: " + args);

		CliMain.main(args.toArray(new String[0]));

		Map<String, ClassNode> nodes = TestUtils.jarToClassNodes(jarpath);
		nodes.putAll(TestUtils.filesToClassNodes(directfiles.values()));
		nodes.putAll(TestUtils.filesToClassNodes(cdirfiles.values()));

		for (ClassNode cn : nodes.values()) {
			assertNull(TestUtils.getClInitMethod(cn));
		}
		for (FieldNode fn : TestUtils.getFields(nodes.get(ConstantsJar.class.getName())).values()) {
			assertNonNull(fn.value, fn.name);
		}
		for (FieldNode fn : TestUtils.getFields(nodes.get(ConstantsClassDir.class.getName())).values()) {
			assertNonNull(fn.value, fn.name);
		}
		for (FieldNode fn : TestUtils.getFields(nodes.get(ConstantsClassFile.class.getName())).values()) {
			assertNonNull(fn.value, fn.name);
		}
	}

	public static class ConstantsJar {
		public static final int INTVAL = Integer.parseInt("10");
		public static final long TIME = getTime();
		public static final long RANDOM = ConstantClassPath.getRandom();

		@ConstantExpression
		public static long getTime() {
			return System.currentTimeMillis();
		}
	}

	public static class ConstantsClassDir {
		public static final int INTVAL = Integer.parseInt("10");
		public static final long TIME = getTime();
		public static final long RANDOM = ConstantClassPath.getRandom();

		@ConstantExpression
		public static long getTime() {
			return System.currentTimeMillis();
		}
	}

	public static class ConstantsClassFile {
		public static final int INTVAL = Integer.parseInt("10");
		public static final long TIME = getTime();
		public static final long RANDOM = ConstantClassPath.getRandom();

		@ConstantExpression
		public static long getTime() {
			return System.currentTimeMillis();
		}
	}

	public static class ConstantClassPath {
		@ConstantExpression
		public static long getRandom() {
			return new Random().nextLong();
		}
	}

}
