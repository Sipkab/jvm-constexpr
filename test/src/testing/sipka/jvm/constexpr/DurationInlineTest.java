package testing.sipka.jvm.constexpr;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class DurationInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(C1.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, C1.class);
			assertFalse(TestUtils.isContainsInvokeStatic(TestUtils.getClInitMethod(classnode), Duration.class,
					"ofSeconds", long.class, long.class));
		}
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(C2.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, C2.class);
			TestUtils.assertNoInvokeStaticInClInit(classnode);
		}
	}

	public static class C1 {
		public static final Duration D1 = Duration.ofSeconds(1, 0);
		public static final Duration D2 = Duration.ofSeconds(2);
		public static final Duration D3 = Duration.ofDays(3);
		public static final Duration D4 = Duration.ofHours(4);
		public static final Duration D5 = Duration.ofMillis(5);
		public static final Duration D6 = Duration.ofMinutes(6);
		public static final Duration D7 = Duration.ofNanos(7);
		public static final Duration D8 = Duration.ofSeconds(1, 2);
		public static final Duration D9 = Duration.ofSeconds(Long.MAX_VALUE, 0); // to ofSeconds(9223372036854775807L)
	}

	public static class C2 {
		public static final Duration ZERO = Duration.ofSeconds(0, 0);
	}
}
