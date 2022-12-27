package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests mathematical JVM instructions for constant inlining.
 */
@SakerTest
public class MathOperatorsInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		public static final int INT1;
		public static final int INT4;
		public static final int INT5;
		public static final int INT1000;
		public static final int INT70000;

		public static final float FLOAT70000_123;
		public static final double DOUBLE70000_123;

		public static final int INT1000ADD4;
		public static final int INT1000SUB4;
		public static final int INT1000MUL4;
		public static final int INT1000DIV4;
		public static final int INT5REM4;
		public static final int INT1000SHL4;
		public static final int INT1000SHR4;
		public static final int INT1000USHR4;
		public static final int INT5AND4;
		public static final int INT1000OR4;
		public static final int INT5XOR4;

		public static final long LONG1;
		public static final long LONG4;
		public static final long LONG5;
		public static final long LONG1000;

		public static final long LONG1000ADD4;
		public static final long LONG1000SUB4;
		public static final long LONG1000MUL4;
		public static final long LONG1000DIV4;
		public static final long LONG5REM4;
		public static final long LONG1000SHL4;
		public static final long LONG1000SHR4;
		public static final long LONG1000USHR4;
		public static final long LONG1000SHL4_I;
		public static final long LONG1000SHR4_I;
		public static final long LONG1000USHR4_I;
		public static final long LONG5AND4;
		public static final long LONG1000OR4;
		public static final long LONG5XOR4;

		public static final long I2L;
		public static final float I2F;
		public static final double I2D;
		public static final int L2I;
		public static final float L2F;
		public static final double L2D;
		public static final int F2I;
		public static final long F2L;
		public static final double F2D;
		public static final int D2I;
		public static final long D2L;
		public static final float D2F;
		public static final byte I2B;
		public static final char I2C;
		public static final short I2S;

		public static final int INEG;
		public static final long LNEG;
		public static final float FNEG;
		public static final double DNEG;

		public static final int INTBITNEG;
		public static final long LONGBITNEG;

		static {
			INT1 = 1;
			INT4 = 4;
			INT5 = 5;
			INT1000 = 1000;
			INT70000 = 70000;

			//or with some big value, so they are in long range
			LONG1 = 1L | 0x00f00000_00000000L;
			LONG4 = 4L | 0x00f00000_00000000L;
			LONG5 = 5L | 0x00f00000_00000000L;
			LONG1000 = 1000L | 0x00f00000_00000000L;

			FLOAT70000_123 = 70000.123f;
			DOUBLE70000_123 = 70000.123d;

			INT1000ADD4 = INT1000 + INT4;
			INT1000SUB4 = INT1000 - INT4;
			INT1000MUL4 = INT1000 * INT4;
			INT1000DIV4 = INT1000 / INT4;
			INT5REM4 = INT5 % INT4;
			INT1000SHL4 = INT1000 << INT4;
			INT1000SHR4 = INT1000 >> INT4;
			INT1000USHR4 = INT1000 >>> INT4;
			INT5AND4 = INT5 & INT4;
			INT1000OR4 = INT1000 | INT4;
			INT5XOR4 = INT5 ^ INT4;

			LONG1000ADD4 = LONG1000 + LONG4;
			LONG1000SUB4 = LONG1000 - LONG4;
			LONG1000MUL4 = LONG1000 * LONG4;
			LONG1000DIV4 = LONG1000 / LONG4;
			LONG5REM4 = LONG5 % LONG4;
			LONG1000SHL4 = LONG1000 << LONG4;
			LONG1000SHR4 = LONG1000 >> LONG4;
			LONG1000USHR4 = LONG1000 >>> LONG4;
			LONG1000SHL4_I = LONG1000 << INT4;
			LONG1000SHR4_I = LONG1000 >> INT4;
			LONG1000USHR4_I = LONG1000 >>> INT4;
			LONG5AND4 = LONG5 & LONG4;
			LONG1000OR4 = LONG1000 | LONG4;
			LONG5XOR4 = LONG5 ^ LONG4;

			I2L = INT70000;
			I2F = INT70000;
			I2D = INT70000;
			L2I = (int) LONG1000;
			L2F = LONG1000;
			L2D = LONG1000;
			F2I = (int) FLOAT70000_123;
			F2L = (long) FLOAT70000_123;
			F2D = FLOAT70000_123;
			D2I = (int) DOUBLE70000_123;
			D2L = (long) DOUBLE70000_123;
			D2F = (float) DOUBLE70000_123;
			I2B = (byte) INT70000;
			I2C = (char) INT70000;
			I2S = (short) INT70000;

			INEG = -INT1;
			LNEG = -LONG1;
			FNEG = -FLOAT70000_123;
			DNEG = -DOUBLE70000_123;

			INTBITNEG = ~INT1000;
			LONGBITNEG = ~LONG1000;
		}

	}
}
