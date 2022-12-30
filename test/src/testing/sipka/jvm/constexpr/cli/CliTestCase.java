package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import testing.saker.SakerTestCase;

public abstract class CliTestCase extends SakerTestCase {

	protected Path testCaseDirectory;

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		testCaseDirectory = Paths.get(parameters.get("TestsBaseBuildDirectory")).resolve(getClass().getSimpleName());
		runTestImpl(parameters);
	}

	protected abstract void runTestImpl(Map<String, String> parameters) throws Throwable;

}
