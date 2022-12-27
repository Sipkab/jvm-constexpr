package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that {@link Class#getCanonicalName()} is inlined properly
 */
@SakerTest
public class ClassCanonicalNameInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		TestUtils.assertSameStaticFieldValues(outputs.firstEntry().getValue(), Constants.class);
	}

	public static class Constants {
		public static final String classNameVoid;
		public static final String classNameString;
		public static final String classNameByte;
		public static final String classNameShort;
		public static final String classNameInteger;
		public static final String classNameLong;
		public static final String classNameFloat;
		public static final String classNameDouble;
		public static final String classNameCharacter;
		public static final String classNameBoolean;

		public static final String classNamePrimitiveVoid;
		public static final String classNamePrimitiveByte;
		public static final String classNamePrimitiveShort;
		public static final String classNamePrimitiveInteger;
		public static final String classNamePrimitiveLong;
		public static final String classNamePrimitiveFloat;
		public static final String classNamePrimitiveDouble;
		public static final String classNamePrimitiveCharacter;
		public static final String classNamePrimitiveBoolean;

		public static final String classNameWrapperTypeVoid;
		public static final String classNameWrapperTypeByte;
		public static final String classNameWrapperTypeShort;
		public static final String classNameWrapperTypeInteger;
		public static final String classNameWrapperTypeLong;
		public static final String classNameWrapperTypeFloat;
		public static final String classNameWrapperTypeDouble;
		public static final String classNameWrapperTypeCharacter;
		public static final String classNameWrapperTypeBoolean;

		public static final String classNamePrimitiveArrayByte;
		public static final String classNamePrimitiveArrayShort;
		public static final String classNamePrimitiveArrayInteger;
		public static final String classNamePrimitiveArrayLong;
		public static final String classNamePrimitiveArrayFloat;
		public static final String classNamePrimitiveArrayDouble;
		public static final String classNamePrimitiveArrayCharacter;
		public static final String classNamePrimitiveArrayBoolean;
		public static final String classNameArrayObject;

		public static final String classNamePrimitiveArrayArrayByte;
		public static final String classNamePrimitiveArrayArrayShort;
		public static final String classNamePrimitiveArrayArrayInteger;
		public static final String classNamePrimitiveArrayArrayLong;
		public static final String classNamePrimitiveArrayArrayFloat;
		public static final String classNamePrimitiveArrayArrayDouble;
		public static final String classNamePrimitiveArrayArrayCharacter;
		public static final String classNamePrimitiveArrayArrayBoolean;
		public static final String classNameArrayArrayObject;

		public static final String classNameInner;
		public static final String classNameArrayInner;
		public static final String classNameArrayArrayInner;

		static {
			classNameVoid = Void.class.getCanonicalName();
			classNameString = String.class.getCanonicalName();
			classNameByte = Byte.class.getCanonicalName();
			classNameShort = Short.class.getCanonicalName();
			classNameInteger = Integer.class.getCanonicalName();
			classNameLong = Long.class.getCanonicalName();
			classNameFloat = Float.class.getCanonicalName();
			classNameDouble = Double.class.getCanonicalName();
			classNameCharacter = Character.class.getCanonicalName();
			classNameBoolean = Boolean.class.getCanonicalName();

			classNamePrimitiveVoid = void.class.getCanonicalName();
			classNamePrimitiveByte = byte.class.getCanonicalName();
			classNamePrimitiveShort = short.class.getCanonicalName();
			classNamePrimitiveInteger = int.class.getCanonicalName();
			classNamePrimitiveLong = long.class.getCanonicalName();
			classNamePrimitiveFloat = float.class.getCanonicalName();
			classNamePrimitiveDouble = double.class.getCanonicalName();
			classNamePrimitiveCharacter = char.class.getCanonicalName();
			classNamePrimitiveBoolean = boolean.class.getCanonicalName();

			classNameWrapperTypeVoid = Void.TYPE.getCanonicalName();
			classNameWrapperTypeByte = Byte.TYPE.getCanonicalName();
			classNameWrapperTypeShort = Short.TYPE.getCanonicalName();
			classNameWrapperTypeInteger = Integer.TYPE.getCanonicalName();
			classNameWrapperTypeLong = Long.TYPE.getCanonicalName();
			classNameWrapperTypeFloat = Float.TYPE.getCanonicalName();
			classNameWrapperTypeDouble = Double.TYPE.getCanonicalName();
			classNameWrapperTypeCharacter = Character.TYPE.getCanonicalName();
			classNameWrapperTypeBoolean = Boolean.TYPE.getCanonicalName();

			classNamePrimitiveArrayByte = byte[].class.getCanonicalName();
			classNamePrimitiveArrayShort = short[].class.getCanonicalName();
			classNamePrimitiveArrayInteger = int[].class.getCanonicalName();
			classNamePrimitiveArrayLong = long[].class.getCanonicalName();
			classNamePrimitiveArrayFloat = float[].class.getCanonicalName();
			classNamePrimitiveArrayDouble = double[].class.getCanonicalName();
			classNamePrimitiveArrayCharacter = char[].class.getCanonicalName();
			classNamePrimitiveArrayBoolean = boolean[].class.getCanonicalName();
			classNameArrayObject = Object[].class.getCanonicalName();

			classNamePrimitiveArrayArrayByte = byte[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayShort = short[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayInteger = int[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayLong = long[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayFloat = float[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayDouble = double[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayCharacter = char[][].class.getCanonicalName();
			classNamePrimitiveArrayArrayBoolean = boolean[][].class.getCanonicalName();
			classNameArrayArrayObject = Object[][].class.getCanonicalName();

			classNameInner = InnerClass.class.getCanonicalName();
			classNameArrayInner = InnerClass[].class.getCanonicalName();
			classNameArrayArrayInner = InnerClass[][].class.getCanonicalName();
		}

		public static class InnerClass {

		}
	}
}
