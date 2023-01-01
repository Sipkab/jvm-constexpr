package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.main.CliMain;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

/**
 * Tests that the CLI main can be run on .class files, and that they are properly loaded by the created classloader.
 */
@SakerTest
public class ClassDryRunCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Map<Class<?>, Path> cfiles = TestUtils.writeClasses(testCaseDirectory, Constants.class,
				ConstantClassPath.class);

		List<String> args = new ArrayList<>();
		for (Path path : cfiles.values()) {
			args.add("-input");
			args.add(path.toString());
		}

		//just check that it runs, and no classloading errors happen
		CliMain.main(args.toArray(new String[0]));
	}

	public static class Constants {
		public static final int INTVAL = Integer.parseInt("10");
		public static final long TIME = getTime();
		public static final long RANDOM = ConstantClassPath.getRandom();

		public static final ConstantClassPath somefield = new ConstantClassPath();

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
