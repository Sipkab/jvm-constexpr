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
 * Tests that the CLI main can be run on JARs, and that they are properly overwritten.
 */
@SakerTest
public class JarOverwriteCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path jarpath = testCaseDirectory.resolve("classes.jar");
		TestUtils.writeJar(jarpath, Constants.class, ConstantClassPath.class);

		List<String> args = new ArrayList<>();
		args.add("-input");
		args.add(jarpath.toString());
		args.add("-overwrite");

		//just check that it runs, and no classloading errors happen
		CliMain.main(args.toArray(new String[0]));

		Map<String, ClassNode> nodes = TestUtils.jarToClassNodes(jarpath);
		for (ClassNode cn : nodes.values()) {
			assertNull(TestUtils.getClInitMethod(cn));
		}
		for (FieldNode fn : TestUtils.getFields(nodes.get(Constants.class.getName())).values()) {
			assertNonNull(fn.value, fn.name);
		}
	}

	public static class Constants {
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
