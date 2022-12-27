package testing.sipka.jvm.constexpr;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that some enum methods are inlined properly
 */
@SakerTest
public class BasicEnumInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		public static final String VALUEOF = DayOfWeek.valueOf("MONDAY").name();
		public static final String NAME = DayOfWeek.MONDAY.name();
		public static final String NAME2 = TimeUnit.DAYS.name();
		public static final String NAME3 = TimeUnit.valueOf("DAYS").name();
	}
}
