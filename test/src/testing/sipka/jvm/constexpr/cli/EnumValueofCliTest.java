package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

/**
 * Checks that the valueOf(String) returns EnumType method is added automatically as a reconstructor.
 */
@SakerTest
public class EnumValueofCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path injarpath = testCaseDirectory.resolve("classes.jar");
		Path outjarpath = testCaseDirectory.resolve("out.jar");
		TestUtils.writeJar(injarpath, Constants.class, CliEnumType.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-output", outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());

		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		public static final boolean ENEQ = CliEnumType.FIRST.equals(CliEnumType.SECOND);
		public static final boolean VALOFEQ = CliEnumType.FIRST.equals(CliEnumType.valueOf("SECOND"));
	}

	public static enum CliEnumType {
		FIRST,
		SECOND;

	}
}
