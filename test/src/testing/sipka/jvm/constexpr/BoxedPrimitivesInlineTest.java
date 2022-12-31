package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that boxed primitives are treated as constants.
 */
@SakerTest
public class BoxedPrimitivesInlineTest extends SakerTestCase {

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
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(BoxedConstants.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, BoxedConstants.class);
		}
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(ConstructorReplacement.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, ConstructorReplacement.class);
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "makeByte", "()Ljava/lang/Byte;"),
					Opcodes.INVOKESPECIAL);
		}
	}

	@SuppressWarnings("deprecation") // for new Primitive() constructors
	public static class Constants {
		public static final byte hexByte;
		public static final byte strByte;
		public static final byte valueOfByte;
		public static final byte autoBoxByte;
		public static final byte objectPrimitiveByte;
		public static final byte parseByte;
		public static final byte newByte = new Byte("123");
		static {
			hexByte = Byte.valueOf("22", 16);
			strByte = Byte.valueOf("111");
			valueOfByte = Byte.valueOf((byte) 123);
			autoBoxByte = (Byte) (byte) 456;
			objectPrimitiveByte = (byte) (Object) (byte) 123;
			parseByte = Byte.parseByte("123");
		}

		public static final short hexShort;
		public static final short strShort;
		public static final short valueOfShort;
		public static final short autoBoxShort;
		public static final short objectPrimitiveShort;
		public static final short parseShort;
		public static final short newShort = new Short("123");
		static {
			hexShort = Short.valueOf("22", 16);
			strShort = Short.valueOf("111");
			valueOfShort = Short.valueOf((short) 123);
			autoBoxShort = (Short) (short) 456;
			objectPrimitiveShort = (short) (Object) (short) 123;
			parseShort = Short.parseShort("12345");
		}

		public static final int hexInteger;
		public static final int strInteger;
		public static final int valueOfInteger;
		public static final int autoBoxInteger;
		public static final int objectPrimitiveInteger;
		public static final int parseInt;
		public static final int newInt = new Integer("123");
		static {
			hexInteger = Integer.valueOf("22", 11);
			strInteger = Integer.valueOf("111");
			valueOfInteger = Integer.valueOf(123);
			autoBoxInteger = (Integer) 456;
			objectPrimitiveInteger = (int) (Object) 123;
			parseInt = Integer.parseInt("12345678");
		}

		public static final long hexLong;
		public static final long strLong;
		public static final long valueOfLong;
		public static final long autoBoxLong;
		public static final long objectPrimitiveLong;
		public static final long parseLong;
		public static final long newLong = new Long("123");
		static {
			hexLong = Long.valueOf("22", 16);
			strLong = Long.valueOf("111");
			valueOfLong = Long.valueOf(123L);
			autoBoxLong = (Long) 456L;
			objectPrimitiveLong = (long) (Object) 123L;
			parseLong = Long.parseLong("12345678910123");
		}

		public static final float strFloat;
		public static final float valueOfFloat;
		public static final float autoBoxFloat;
		public static final float objectPrimitiveFloat;
		public static final float parseFloat;
		public static final float newFloat = new Float("123.4");
		public static final float floatNaN;
		public static final float floatPosInf;
		public static final float floatNegInf;
		static {
			strFloat = Float.valueOf("111");
			valueOfFloat = Float.valueOf(123L);
			autoBoxFloat = (Float) 456f;
			objectPrimitiveFloat = (float) (Object) 123f;
			parseFloat = Float.parseFloat("123.456");
			floatNaN = 0.0f / 0.0f;
			floatPosInf = 1.0f / 0.0f;
			floatNegInf = -1.0f / 0.0f;
		}

		public static final double strDouble;
		public static final double valueOfDouble;
		public static final double autoBoxDouble;
		public static final double objectPrimitiveDouble;
		public static final double parseDouble;
		public static final double newDouble = new Double("123.45");
		public static final double doubleNaN;
		public static final double doublePosInf;
		public static final double doubleNegInf;
		static {
			strDouble = Double.valueOf("111");
			valueOfDouble = Double.valueOf(123L);
			autoBoxDouble = (Double) 456d;
			objectPrimitiveDouble = (double) (Object) 123d;
			parseDouble = Double.parseDouble("123.4567890123456789");
			doubleNaN = 0.0d / 0.0d;
			doublePosInf = 1.0d / 0.0d;
			doubleNegInf = -1.0d / 0.0d;
		}

		public static final char valueOfCharacter;
		public static final char autoBoxCharacter;
		public static final char objectPrimitiveCharacter;
		public static final char newChar = new Character('x');
		static {
			valueOfCharacter = Character.valueOf('X');
			autoBoxCharacter = (Character) 'Y';
			objectPrimitiveCharacter = (char) (Object) 'Z';
		}

		public static final boolean valueOfBoolean;
		public static final boolean strBoolean;
		public static final boolean autoBoxBoolean;
		public static final boolean objectPrimitiveBoolean;
		public static final boolean newBoolean = new Boolean("true");
		static {
			valueOfBoolean = Boolean.valueOf(true);
			strBoolean = Boolean.valueOf("true");
			autoBoxBoolean = (Boolean) true;
			objectPrimitiveBoolean = (boolean) (Object) true;
		}
	}

	public static class BoxedConstants {
		public static final Byte BOXEDBYTE = 123;
		public static final Short BOXEDSHORT = 999;
		public static final Integer BOXEDINTEGER = 999;
		public static final Long BOXEDLONG = 999L;
		public static final Float BOXEDFLOAT = 999f;
		public static final Double BOXEDDOUBLE = 999d;
		public static final Boolean BOXEDBOOLEAN = true;
		public static final Character BOXEDCHARACTER = 'X';

		static {
			//to test that boxed instances are not inlined as constants
			//bytecode validation would fail
			if (Byte.valueOf((byte) 123) == BOXEDBYTE) {
				System.out.println("TESTBYTE");
			}
			if (Short.valueOf((short) 123) == BOXEDSHORT) {
				System.out.println("TESTSHORT");
			}
			if (Integer.valueOf(123) == BOXEDINTEGER) {
				System.out.println("TESTINT");
			}
			if (Long.valueOf(123) == BOXEDLONG) {
				System.out.println("TESTLONG");
			}
			if (Float.valueOf(123) == BOXEDFLOAT) {
				System.out.println("TESTFLOAT");
			}
			if (Double.valueOf(123) == BOXEDDOUBLE) {
				System.out.println("TESTDOUBLE");
			}
			if (Boolean.valueOf(false) == BOXEDBOOLEAN) {
				System.out.println("TESTBOOLEAN");
			}
			if (Character.valueOf('Y') == BOXEDCHARACTER) {
				System.out.println("TESTCHARACTER");
			}
		}
	}

	public static class ConstructorReplacement {
		public Byte makeByte() {
			return new Byte("111");
		}
	}
}
