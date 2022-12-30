package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

@SakerTest
public class SimpleClassPathConstantExpressionCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path injarpath = testCaseDirectory.resolve("classes.jar");
		Path cpjarpath = testCaseDirectory.resolve("classpath.jar");
		Path outjarpath = testCaseDirectory.resolve("out.jar");
		TestUtils.writeJar(injarpath, Constants.class);
		TestUtils.writeJar(cpjarpath, ClassPathClass.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-classpath", cpjarpath.toString(), "-output",
				outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
		assertNonNull(classnode.fields.get(0).value, "optimized field");

		//classpath classes shouldnt be added to the output jar
		assertException(NoSuchFileException.class,
				() -> TestUtils.loadClassNodeFromJar(outjarpath, ClassPathClass.class.getName()));
	}

	public static class ClassPathClass {
		//this should not be optimized, as its on the classpath
		@ConstantExpression
		public static final long CLASSPATHCONSTANT = getTime();

		public static long getTime() {
			return System.currentTimeMillis();
		}
	}

	public static class Constants {
		@ConstantExpression
		public static final long BUILDTIME = ClassPathClass.getTime();

	}

}
