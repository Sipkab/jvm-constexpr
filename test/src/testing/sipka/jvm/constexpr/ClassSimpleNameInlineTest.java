package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that {@link Class#getSimpleName()} is inlined properly
 */
@SakerTest
public class ClassSimpleNameInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
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
			classNameVoid = Void.class.getSimpleName();
			classNameString = String.class.getSimpleName();
			classNameByte = Byte.class.getSimpleName();
			classNameShort = Short.class.getSimpleName();
			classNameInteger = Integer.class.getSimpleName();
			classNameLong = Long.class.getSimpleName();
			classNameFloat = Float.class.getSimpleName();
			classNameDouble = Double.class.getSimpleName();
			classNameCharacter = Character.class.getSimpleName();
			classNameBoolean = Boolean.class.getSimpleName();

			classNamePrimitiveVoid = void.class.getSimpleName();
			classNamePrimitiveByte = byte.class.getSimpleName();
			classNamePrimitiveShort = short.class.getSimpleName();
			classNamePrimitiveInteger = int.class.getSimpleName();
			classNamePrimitiveLong = long.class.getSimpleName();
			classNamePrimitiveFloat = float.class.getSimpleName();
			classNamePrimitiveDouble = double.class.getSimpleName();
			classNamePrimitiveCharacter = char.class.getSimpleName();
			classNamePrimitiveBoolean = boolean.class.getSimpleName();

			classNameWrapperTypeVoid = Void.TYPE.getSimpleName();
			classNameWrapperTypeByte = Byte.TYPE.getSimpleName();
			classNameWrapperTypeShort = Short.TYPE.getSimpleName();
			classNameWrapperTypeInteger = Integer.TYPE.getSimpleName();
			classNameWrapperTypeLong = Long.TYPE.getSimpleName();
			classNameWrapperTypeFloat = Float.TYPE.getSimpleName();
			classNameWrapperTypeDouble = Double.TYPE.getSimpleName();
			classNameWrapperTypeCharacter = Character.TYPE.getSimpleName();
			classNameWrapperTypeBoolean = Boolean.TYPE.getSimpleName();

			classNamePrimitiveArrayByte = byte[].class.getSimpleName();
			classNamePrimitiveArrayShort = short[].class.getSimpleName();
			classNamePrimitiveArrayInteger = int[].class.getSimpleName();
			classNamePrimitiveArrayLong = long[].class.getSimpleName();
			classNamePrimitiveArrayFloat = float[].class.getSimpleName();
			classNamePrimitiveArrayDouble = double[].class.getSimpleName();
			classNamePrimitiveArrayCharacter = char[].class.getSimpleName();
			classNamePrimitiveArrayBoolean = boolean[].class.getSimpleName();
			classNameArrayObject = Object[].class.getSimpleName();

			classNamePrimitiveArrayArrayByte = byte[][].class.getSimpleName();
			classNamePrimitiveArrayArrayShort = short[][].class.getSimpleName();
			classNamePrimitiveArrayArrayInteger = int[][].class.getSimpleName();
			classNamePrimitiveArrayArrayLong = long[][].class.getSimpleName();
			classNamePrimitiveArrayArrayFloat = float[][].class.getSimpleName();
			classNamePrimitiveArrayArrayDouble = double[][].class.getSimpleName();
			classNamePrimitiveArrayArrayCharacter = char[][].class.getSimpleName();
			classNamePrimitiveArrayArrayBoolean = boolean[][].class.getSimpleName();
			classNameArrayArrayObject = Object[][].class.getSimpleName();

			classNameInner = InnerClass.class.getSimpleName();
			classNameArrayInner = InnerClass[].class.getSimpleName();
			classNameArrayArrayInner = InnerClass[][].class.getSimpleName();
		}

		public static class InnerClass {

		}
	}
}
