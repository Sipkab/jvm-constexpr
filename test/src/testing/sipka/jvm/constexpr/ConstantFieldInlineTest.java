package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests field force inlining
 */
@SakerTest
public class ConstantFieldInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		opts.setConstantFields(Arrays.asList(TestUtils.getFields(Constants.class, "computed", "millis",
				"random_uuid", "random_str", "format", "objformat")));
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		//the UUID should be replaced with a non random value
		assertFalse(TestUtils.isContainsInvokeStatic(TestUtils.getClInitMethod(classnode), UUID.class, "randomUUID"),
				"randomUUID called");
		assertFalse(TestUtils.isContainsInvokeVirtual(TestUtils.getClInitMethod(classnode), Random.class, "nextInt"),
				"nextInt called");
		assertFalse(TestUtils.isContainsInvokeVirtual(TestUtils.getClInitMethod(classnode), StringBuilder.class,
				"append", int.class), "append called");
		assertFalse(TestUtils.isContainsInvokeStatic(TestUtils.getClInitMethod(classnode), String.class, "format",
				String.class, Object[].class), "String.format called");

		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final long computed = computer();
		//lets hope we don't run this test exactly at new year (+- leap days), and run into a concurrency bug due to the milliseconds flipping
		public static final long millis = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(365);

		public static final UUID random_uuid = UUID.randomUUID();

		public static final String random_str = "a" + new Random().nextInt() + "b";

		//not usually inlined, because its locale dependent, but should be for force inlined field
		public static final String format = String.format("%03d", 1);
		public static final Object objformat = String.format("%03d", 999);

		public static final String concat1 = String.format((Locale) null, "%s %s", format, "X");
		public static final String concat2 = format + " Y";
		public static final String concat3 = objformat + " Z";

		private static long computer() {
			return Constants.class.getName().hashCode();
		}
	}
}
