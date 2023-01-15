package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that {@link Class#getName()} is inlined properly
 */
@SakerTest
public class ClassNameInlineTest extends SakerTestCase {

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
			//the loading of SomeOtherClass would fail, so the constant couldnt be loaded
			//this is fixed by not attempting to load the classes themselves in these simple cases
			InlinerOptions options = TestUtils.createOptionsForClasses(NotAvailableClassNameConstants.class);
			options.setClassLoader(null);
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(options);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, NotAvailableClassNameConstants.class);
			assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
		}
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
			classNameVoid = Void.class.getName();
			classNameString = String.class.getName();
			classNameByte = Byte.class.getName();
			classNameShort = Short.class.getName();
			classNameInteger = Integer.class.getName();
			classNameLong = Long.class.getName();
			classNameFloat = Float.class.getName();
			classNameDouble = Double.class.getName();
			classNameCharacter = Character.class.getName();
			classNameBoolean = Boolean.class.getName();

			classNamePrimitiveVoid = void.class.getName();
			classNamePrimitiveByte = byte.class.getName();
			classNamePrimitiveShort = short.class.getName();
			classNamePrimitiveInteger = int.class.getName();
			classNamePrimitiveLong = long.class.getName();
			classNamePrimitiveFloat = float.class.getName();
			classNamePrimitiveDouble = double.class.getName();
			classNamePrimitiveCharacter = char.class.getName();
			classNamePrimitiveBoolean = boolean.class.getName();

			classNameWrapperTypeVoid = Void.TYPE.getName();
			classNameWrapperTypeByte = Byte.TYPE.getName();
			classNameWrapperTypeShort = Short.TYPE.getName();
			classNameWrapperTypeInteger = Integer.TYPE.getName();
			classNameWrapperTypeLong = Long.TYPE.getName();
			classNameWrapperTypeFloat = Float.TYPE.getName();
			classNameWrapperTypeDouble = Double.TYPE.getName();
			classNameWrapperTypeCharacter = Character.TYPE.getName();
			classNameWrapperTypeBoolean = Boolean.TYPE.getName();

			classNamePrimitiveArrayByte = byte[].class.getName();
			classNamePrimitiveArrayShort = short[].class.getName();
			classNamePrimitiveArrayInteger = int[].class.getName();
			classNamePrimitiveArrayLong = long[].class.getName();
			classNamePrimitiveArrayFloat = float[].class.getName();
			classNamePrimitiveArrayDouble = double[].class.getName();
			classNamePrimitiveArrayCharacter = char[].class.getName();
			classNamePrimitiveArrayBoolean = boolean[].class.getName();
			classNameArrayObject = Object[].class.getName();

			classNamePrimitiveArrayArrayByte = byte[][].class.getName();
			classNamePrimitiveArrayArrayShort = short[][].class.getName();
			classNamePrimitiveArrayArrayInteger = int[][].class.getName();
			classNamePrimitiveArrayArrayLong = long[][].class.getName();
			classNamePrimitiveArrayArrayFloat = float[][].class.getName();
			classNamePrimitiveArrayArrayDouble = double[][].class.getName();
			classNamePrimitiveArrayArrayCharacter = char[][].class.getName();
			classNamePrimitiveArrayArrayBoolean = boolean[][].class.getName();
			classNameArrayArrayObject = Object[][].class.getName();

			classNameInner = InnerClass.class.getName();
			classNameArrayInner = InnerClass[].class.getName();
			classNameArrayArrayInner = InnerClass[][].class.getName();
		}

		public static class InnerClass {

		}
	}

	public static class NotAvailableClassNameConstants {
		public static final String otherClassName = SomeOtherClass.class.getName();
		public static final String otherArrayClassName = SomeOtherClass[].class.getName();
	}

	public static class SomeOtherClass {

	}
}
