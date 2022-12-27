package testing.sipka.jvm.constexpr;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that {@link StringBuilder} is correctly inlined.
 */
@SakerTest
public class StringBuilderInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
			assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
		}
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(SbCoalesce.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			assertFalse(TestUtils.isContainsInvokeVirtual(TestUtils.getClInitMethod(classnode), StringBuilder.class,
					"append", int.class), "append(int) called");
			assertTrue(TestUtils.isContainsInvokeConstructor(TestUtils.getClInitMethod(classnode), StringBuilder.class,
					String.class), "SB constructor not called called");
			StringBuilder s5 = (StringBuilder) TestUtils.loadClass(classnode).getField("S5").get(null);
			assertEquals(s5.toString(), Boolean.toString(true) + 123 + "_XXX", "S5");

			String s6 = (String) TestUtils.loadClass(classnode).getField("S6").get(null);
			assertEquals(SbCoalesce.S6, s6, "S6");

			StringBuilder sx = (StringBuilder) TestUtils.loadClass(classnode).getField("SX").get(null);
			assertEquals(sx.toString(), Boolean.toString(true) + 123 + "_XXX" + "!!!!!", "SX");
		}
	}

	public static class Constants {
		//the fields are initialized using stringbuild .append chains internally, ensure that they are properly constantized by us
		public static final String S1 = "a" + UUID.nameUUIDFromBytes("abc".getBytes(StandardCharsets.UTF_8)) + "b";
		public static final String S2 = "a" + S1 + "b";
		public static final String S3 = "x" + S1 + "\u0001XXX\u0002" + Integer.valueOf(999) + "\u0001YYY\u0002"
				+ Long.valueOf(777) + "y";
		//this doesn't use string builder, concatenated at compile time
		public static final String S4 = "a" + "b";
		public static final String S5 = Integer.valueOf(123) + String.valueOf(456);

	}

	public static class SbCoalesce {
		public static final StringBuilder S5;
		public static final StringBuilder SX;
		public static final Object S6;

		static {
			//check that this is coalesced into a single new StringBuilder("...") call
			S5 = new StringBuilder().append(true).append(123).append("_XXX");
			SX = new StringBuilder().append(true).append(123).append("_XXX");
			SX.append("!!!!!");
			S6 = "a" + SX + "\u0001XXX\u0002" + Integer.valueOf(999) + "b";
		}
	}
}
