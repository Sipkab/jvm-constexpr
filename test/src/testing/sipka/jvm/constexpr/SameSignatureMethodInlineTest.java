package testing.sipka.jvm.constexpr;

import java.lang.reflect.Member;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the fields of a custom enum can be deconstructed-reconstructed.
 */
@SakerTest
public class SameSignatureMethodInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		Map<Member, ReconstructorPredicate> reconstructors = TestUtils
				.allowAllMembers(Arrays.asList(MyClass.class.getConstructor(), MyEnum.class.getMethod("getOrdinal")));
		reconstructors.put(MyEnum.class.getMethod("ordinal"), ReconstructorPredicate.allowInstanceOf(MyEnum.class));
		reconstructors.put(MyClass.class.getMethod("ordinal"), ReconstructorPredicate.allowInstanceOf(MyClass.class));
		opts.setConstantReconstructors(reconstructors);

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, MyEnum.class, MyEnum.SUBFIELD.getClass(), MyClass.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);
		TestUtils.assertNoInvokeStaticInClInit(classnode);
	}

	public static enum MyEnum {
		FIELD1(123),
		FIELD2(456),
		SUBFIELD(789) {
		};

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

	public static class MyClass {
		public int ordinal() {
			return 99;
		}
	}

	public static class Constants {
		//other ordinal shouldn't be inlined, but myenum should
		public static final int MYENUM_ORD = MyEnum.FIELD2.ordinal();
		public static final int MyClass_ORD = new MyClass().ordinal();
	}

}
