package testing.sipka.jvm.constexpr;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Test constant inlining when the function takes array arguments. (or variadic args)
 */
@SakerTest
public class ArrayParameterInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		public static final String str1;
		public static final String str2;
		public static final String str3;
		public static final String str4;
		public static final String str5;
		public static final String str6;
		public static final String str7 = String.format((Locale) null, "%d %d", (Object[]) new Number[] { 123, 456 });
		public static final String str8 = String.format((Locale) null, "%d %d", new Number[] { 789, 012 });
		static {
			str1 = String.format((Locale) null, "%d", 111);
			str2 = String.format((Locale) null, "%d %d", 123, 456);
			str3 = String.format((Locale) null, "%d %d %d %s", (short) 123, 456L, Integer.MAX_VALUE, "abc");
			str4 = String.format((Locale) null, "%d %d %s", new Object[] { (short) 123, 456L, "abc" });
			str5 = String.format((Locale) null, "%d %d", Integer.valueOf(123), Integer.valueOf(456));
			str6 = String.format((Locale) null, "%s %s", null, Integer.valueOf(456));
		}

	}
}
