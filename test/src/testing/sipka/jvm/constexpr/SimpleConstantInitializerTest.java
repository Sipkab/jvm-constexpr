package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that static final constants are set correctly based on the static initializer code.
 * <p>
 * Also that static final field with a value are not modified.
 */
@SakerTest
public class SimpleConstantInitializerTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {

		public static final byte byteDirectM2 = -2;
		public static final byte byteDirectM1 = -1;
		public static final byte byteDirect0 = 0;
		public static final byte byteDirect1 = 1;
		public static final byte byteDirect2 = 2;
		public static final byte byteDirect3 = 3;
		public static final byte byteDirect4 = 4;
		public static final byte byteDirect5 = 5;
		public static final byte byteDirect6 = 6;

		public static final short shortDirectM2 = -2;
		public static final short shortDirectM1 = -1;
		public static final short shortDirect0 = 0;
		public static final short shortDirect1 = 1;
		public static final short shortDirect2 = 2;
		public static final short shortDirect3 = 3;
		public static final short shortDirect4 = 4;
		public static final short shortDirect5 = 5;
		public static final short shortDirect6 = 6;

		public static final int intDirectM2 = -2;
		public static final int intDirectM1 = -1;
		public static final int intDirect0 = 0;
		public static final int intDirect1 = 1;
		public static final int intDirect2 = 2;
		public static final int intDirect3 = 3;
		public static final int intDirect4 = 4;
		public static final int intDirect5 = 5;
		public static final int intDirect6 = 6;

		public static final long longDirectM2 = -2;
		public static final long longDirectM1 = -1;
		public static final long longDirect0 = 0;
		public static final long longDirect1 = 1;
		public static final long longDirect2 = 2;
		public static final long longDirect3 = 3;
		public static final long longDirect4 = 4;
		public static final long longDirect5 = 5;
		public static final long longDirect6 = 6;

		public static final boolean booleanDirectTrue = true;
		public static final boolean booleanDirectFalse = false;

		public static final float floatDirectM1 = -1;
		public static final float floatDirect0 = 0;
		public static final float floatDirect1 = 1;
		public static final float floatDirect2 = 2;
		public static final float floatDirect3 = 3;

		public static final double doubleDirectM1 = -1;
		public static final double doubleDirect0 = 0;
		public static final double doubleDirect1 = 1;
		public static final double doubleDirect2 = 2;
		public static final double doubleDirect3 = 3;

		public static final char charDirect0 = 0;
		public static final char charDirect1 = 1;
		public static final char charDirect2 = 2;
		public static final char charDirect3 = 3;
		public static final char charDirect4 = 4;
		public static final char charDirect5 = 5;
		public static final char charDirect6 = 6;
		public static final char charDirectX = 'X';

		public static final String stringDirect = "directstr";

		public static final byte byteClinitM2;
		public static final byte byteClinitM1;
		public static final byte byteClinit0;
		public static final byte byteClinit1;
		public static final byte byteClinit2;
		public static final byte byteClinit3;
		public static final byte byteClinit4;
		public static final byte byteClinit5;
		public static final byte byteClinit6;

		public static final short shortClinitM2;
		public static final short shortClinitM1;
		public static final short shortClinit0;
		public static final short shortClinit1;
		public static final short shortClinit2;
		public static final short shortClinit3;
		public static final short shortClinit4;
		public static final short shortClinit5;
		public static final short shortClinit6;

		public static final int intClinitM2;
		public static final int intClinitM1;
		public static final int intClinit0;
		public static final int intClinit1;
		public static final int intClinit2;
		public static final int intClinit3;
		public static final int intClinit4;
		public static final int intClinit5;
		public static final int intClinit6;

		public static final long longClinitM2;
		public static final long longClinitM1;
		public static final long longClinit0;
		public static final long longClinit1;
		public static final long longClinit2;
		public static final long longClinit3;
		public static final long longClinit4;
		public static final long longClinit5;
		public static final long longClinit6;

		public static final boolean booleanClinitTrue;
		public static final boolean booleanClinitFalse;

		public static final float floatClinitM1;
		public static final float floatClinit0;
		public static final float floatClinit1;
		public static final float floatClinit2;
		public static final float floatClinit3;

		public static final double doubleClinitM1;
		public static final double doubleClinit0;
		public static final double doubleClinit1;
		public static final double doubleClinit2;
		public static final double doubleClinit3;

		public static final char charClinit0;
		public static final char charClinit1;
		public static final char charClinit2;
		public static final char charClinit3;
		public static final char charClinit4;
		public static final char charClinit5;
		public static final char charClinit6;
		public static final char charClinitX;

		public static final String stringClinit;

		static {
			byteClinitM2 = (byte) -2;
			byteClinitM1 = (byte) -1;
			byteClinit0 = (byte) 0;
			byteClinit1 = (byte) 1;
			byteClinit2 = (byte) 2;
			byteClinit3 = (byte) 3;
			byteClinit4 = (byte) 4;
			byteClinit5 = (byte) 5;
			byteClinit6 = (byte) 6;

			shortClinitM2 = (short) -2;
			shortClinitM1 = (short) -1;
			shortClinit0 = (short) 0;
			shortClinit1 = (short) 1;
			shortClinit2 = (short) 2;
			shortClinit3 = (short) 3;
			shortClinit4 = (short) 4;
			shortClinit5 = (short) 5;
			shortClinit6 = (short) 6;

			intClinitM2 = -2;
			intClinitM1 = -1;
			intClinit0 = 0;
			intClinit1 = 1;
			intClinit2 = 2;
			intClinit3 = 3;
			intClinit4 = 4;
			intClinit5 = 5;
			intClinit6 = 6;

			longClinitM2 = (long) -2;
			longClinitM1 = (long) -1;
			longClinit0 = (long) 0;
			longClinit1 = (long) 1;
			longClinit2 = (long) 2;
			longClinit3 = (long) 3;
			longClinit4 = (long) 4;
			longClinit5 = (long) 5;
			longClinit6 = (long) 6;

			booleanClinitTrue = true;
			booleanClinitFalse = false;

			floatClinitM1 = (float) -1;
			floatClinit0 = (float) 0;
			floatClinit1 = (float) 1;
			floatClinit2 = (float) 2;
			floatClinit3 = (float) 3;

			doubleClinitM1 = (double) -1;
			doubleClinit0 = (double) 0;
			doubleClinit1 = (double) 1;
			doubleClinit2 = (double) 2;
			doubleClinit3 = (double) 3;

			charClinit0 = (char) 0;
			charClinit1 = (char) 1;
			charClinit2 = (char) 2;
			charClinit3 = (char) 3;
			charClinit4 = (char) 4;
			charClinit5 = (char) 5;
			charClinit6 = (char) 6;
			charClinitX = 'X';

			stringClinit = "clrinitstr";
		}
	}
}
