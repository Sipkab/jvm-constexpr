package testing.sipka.jvm.constexpr;

import java.time.Period;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class PeriodInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(C1.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, C1.class);
			assertFalse(TestUtils.isContainsInvokeStatic(TestUtils.getClInitMethod(classnode), Period.class, "of",
					int.class, int.class, int.class));
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
		public static final Period YEARS = Period.of(1, 0, 0);
		public static final Period MONTHS = Period.of(0, 2, 0);
		public static final Period DAYS = Period.of(0, 0, 3);
	}

	public static class C2 {
		public static final Period ZERO = Period.of(0, 0, 0);
	}
}
