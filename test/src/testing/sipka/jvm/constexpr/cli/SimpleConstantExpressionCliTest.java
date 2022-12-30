package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

@SakerTest
public class SimpleConstantExpressionCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path injarpath = testCaseDirectory.resolve("classes.jar");
		Path outjarpath = testCaseDirectory.resolve("out.jar");
		TestUtils.writeJar(injarpath, Constants.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-output", outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		@ConstantExpression
		public static final long BUILDTIME = System.currentTimeMillis();

	}

}
