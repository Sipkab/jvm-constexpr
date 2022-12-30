package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.main.CliMain;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

@SakerTest
public class DryRunCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path jarpath = testCaseDirectory.resolve("classes.jar");
		TestUtils.writeJar(jarpath, Constants.class);

		CliMain.main("-input", jarpath.toString());
	}

	public static class Constants {
		public static final int INTVAL = Integer.parseInt("10");
	}

}
