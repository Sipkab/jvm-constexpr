package testing.sipka.jvm.constexpr;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests known {@link String} functions.
 */
@SakerTest
public class StringFunctionsInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {

		public static final int length;
		public static final boolean isEmptyTrue;
		public static final boolean isEmptyFalse;
		public static final char charAt;
		public static final int codePointAt;
		public static final int codePointBefore;
		public static final int codePointCount;

		public static final boolean contains;
		public static final String substring1;
		public static final String substring2;
		public static final String replace;
		public static final String concat;
		public static final String toString;
		public static final String subSequence;

		public static final String toUpperCase;
		public static final String toLowerCase;

		public static final String valueOfInt;
		public static final String valueOfBoolean;
		public static final String valueOfChar;
		public static final String valueOfLong;
		public static final String valueOfFloat;
		public static final String valueOfDouble;
		public static final String valueOfObject;

		static {
			length = "abcd".length();
			isEmptyTrue = "".isEmpty();
			isEmptyFalse = "abcd".isEmpty();
			charAt = "xyz".charAt(1);
			codePointAt = "xyz".codePointAt(1);
			codePointBefore = "xyz".codePointBefore(1);
			codePointCount = "abcxyz".codePointCount(2, 4);

			contains = "abcxyz".contains("cx");
			substring1 = "abcxyz".substring(3);
			substring2 = "abcxyz".substring(4, 5);
			replace = "a.b.c".replace('.', '/');
			concat = "abc".concat("xyz");
			toString = "abc".toString();
			subSequence = (String) "abcxyz".subSequence(1, 4);

			toUpperCase = "abc".toUpperCase(Locale.ROOT);
			toLowerCase = "abc".toLowerCase(Locale.ROOT);

			valueOfInt = String.valueOf(111);
			valueOfBoolean = String.valueOf(true);
			valueOfChar = String.valueOf('X');
			valueOfLong = String.valueOf(222L);
			valueOfFloat = String.valueOf(1.23f);
			valueOfDouble = String.valueOf(4.56d);
			valueOfObject = String.valueOf("strobj");
		}

	}
}
