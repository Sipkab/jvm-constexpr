package testing.sipka.jvm.constexpr;

import java.lang.reflect.Member;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.thirdparty.saker.util.ReflectUtils;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Test array field type related functionality.
 */
@SakerTest
public class ArrayFieldTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		opts.setConstantFields(Arrays.asList(TestUtils.getFields(Constants.class, "FROMSTRING", "CONST_byte",
				"CONST_short", "CONST_int", "CONST_long", "CONST_float", "CONST_double", "CONST_boolean", "CONST_char",
				"CONST_Object", "CONSTEXPRD")));
		List<Member> inlinemethods = new ArrayList<>();
		inlinemethods.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "computer"));
		opts.setConstantReconstructors(TestUtils.allowAllMembers(inlinemethods));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, "B1", "B2", "CMP1", "nCO0", "nCO1", "nCO2");
	}

	public static class Constants {
		public static final byte[] ARRAY;
		public static final byte B1;
		public static final byte B2;

		public static final byte[] FROMSTRING = "FROMSTRING".getBytes(StandardCharsets.UTF_8);
		public static final byte FS1 = FROMSTRING[1];

		public static final byte[] CONST_byte = new byte[] { 1, 2, 3 };
		public static final byte byte1 = CONST_byte[1];

		public static final short[] CONST_short = new short[] { 4, 5, 6 };
		public static final short short1 = CONST_short[1];

		public static final int[] CONST_int = new int[] { 4, 5, 6 };
		public static final int int1 = CONST_int[1];

		public static final long[] CONST_long = new long[] { 4, 5, 6 };
		public static final long long1 = CONST_long[1];

		public static final float[] CONST_float = new float[] { 4, 5, 6 };
		public static final float float1 = CONST_float[1];

		public static final double[] CONST_double = new double[] { 4, 5, 6 };
		public static final double double1 = CONST_double[1];

		public static final boolean[] CONST_boolean = new boolean[] { false, true, true };
		public static final boolean boolean1 = CONST_boolean[1];

		public static final char[] CONST_char = new char[] { 'X', 'Y', 'Z' };
		public static final char char1 = CONST_char[1];

		public static final byte[] COMPUTED = computer();
		public static final byte CMP1 = COMPUTED[1]; // not inlined because COMPUTED is not a constant field
		public static final byte[] CONSTEXPRD = constexprd();
		public static final byte CE1 = CONSTEXPRD[1];

		public static final Object[] CONST_Object = new Number[] { Integer.valueOf(3), 10L, (Double) 3.0d };
		public static final Object[] nonCONST_Object = new Number[] { Integer.valueOf(3), 10L, (Double) 3.0d };
		public static final int CO0 = (int) CONST_Object[0];
		public static final long CO1 = (long) CONST_Object[1];
		public static final double CO2 = (double) CONST_Object[2];

		public static final int nCO0 = (int) nonCONST_Object[0];
		public static final long nCO1 = (long) nonCONST_Object[1];
		public static final double nCO2 = (double) nonCONST_Object[2];

		public static final int ARRGET0 = new int[] { 1, 2, 3 }[0];
		public static final int ARRGET1 = new int[] { 1, 2, 3 }[1];
		public static final int ARRGET2 = new int[] { 1, 2, 3 }[2];
		public static final int ARRGETPI = new int[] { 1, 2, 3 }[Integer.parseInt("2")];
		public static final int ARRGETNV = (new int[3])[2];

		public static final int LENGTH = new int[99].length;
		public static final int LENGTH2 = new int[] { 1, 2, 3 }.length;
		public static final int LENGTH3 = new Number[] { 1, 2, 3 }.length;

		private static byte[] computer() {
			return "COMPUTED".getBytes(StandardCharsets.UTF_8);
		}

		private static byte[] constexprd() {
			return "CONSTEXPRD".getBytes(StandardCharsets.UTF_8);
		}

		static {
			//B1, B2 can't be inlined, as the ARRAY field is not configured as a constant field
			ARRAY = new byte[] { 1, 2, 3 };
			B1 = ARRAY[1];
			ARRAY[1] = 99;
			B2 = ARRAY[1];
		}

	}
}
