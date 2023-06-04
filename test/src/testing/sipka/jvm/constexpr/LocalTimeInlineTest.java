package testing.sipka.jvm.constexpr;

import java.time.LocalTime;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class LocalTimeInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(C1.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, C1.class);
			assertFalse(TestUtils.isContainsInvokeStatic(TestUtils.getClInitMethod(classnode), LocalTime.class, "of",
					int.class, int.class, int.class, int.class));
		}
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(C2.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, C2.class);
			TestUtils.assertNoInvokeStaticInClInit(classnode);
		}
	}

	//check that the last parameters are omitted correctly if they are 0
	public static class C1 {
		public static final LocalTime T1 = LocalTime.of(1, 2, 3, 0);
		public static final LocalTime T2 = LocalTime.of(1, 2, 0, 0);
		public static final LocalTime NOON = LocalTime.of(12, 0, 0, 0);
	}

	public static class C2 {
		public static final LocalTime NOON = LocalTime.of(12, 0, 0, 0);
		public static final LocalTime MIDNIGHT = LocalTime.of(0, 0, 0, 0);
		public static final LocalTime MIN = LocalTime.of(0, 0, 0, 0); // same as midnight
		public static final LocalTime MAX = LocalTime.of(23, 59, 59, 999_999_999);
	}
}
