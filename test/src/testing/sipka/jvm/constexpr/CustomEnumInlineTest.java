package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the fields of a custom enum can be deconstructed-reconstructed.
 */
@SakerTest
public class CustomEnumInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		//set the getter method as a constant reconstructor, so it can be called by the inliner
		opts.setConstantReconstructors(Arrays.asList(Constants.class.getMethod("enumgetter"),
				MyEnum.class.getMethod("getOrdinal"), MyEnum.class.getField("val")));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, MyEnum.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass, "F1NC");
		TestUtils.assertNoInvokeStaticInClInit(classnode);
	}

	public static enum MyEnum {
		FIELD1(123),
		FIELD2(456);

		public final int val;

		private MyEnum(int val) {
			this.val = val;
		}

		public int getOrdinal() {
			return ordinal();
		}

		public String getNonConstant() {
			return name() + "_X";
		}
	}

	public static class Constants {
		public static final MyEnum F1 = enumgetter();
		public static final int F1ord = enumgetter().getOrdinal();
		public static final int F1ord_2 = enumgetter().ordinal();
		public static final int F1EnumOrd = ((Enum<?>) enumgetter()).ordinal();
		public static final String F1EnumName = ((Enum<?>) enumgetter()).name();
		public static final String F1name = enumgetter().name();
		public static final String F1NC = enumgetter().getNonConstant();

		public static final int F1VAL = enumgetter().val;

		public static final String DECLARING_NAME1 = MyEnum.FIELD1.getDeclaringClass().getName();
		public static final String DECLARING_NAME2 = enumgetter().getDeclaringClass().getName();
		public static final String DECLARING_NAME3 = ((Enum<?>) enumgetter()).getDeclaringClass().getName();

		public static MyEnum enumgetter() {
			return MyEnum.FIELD1;
		}
	}

}
