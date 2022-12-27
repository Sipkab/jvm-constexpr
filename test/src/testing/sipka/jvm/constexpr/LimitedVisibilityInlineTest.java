package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the fields/methods/constructors with limited visibility are accessed correctly.
 */
@SakerTest
public class LimitedVisibilityInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		opts.setConstantTypes(Arrays.asList(MyConstantClass.class, MySubClass.class));

		opts.setConstantReconstructors(Arrays.asList(MyConstantClass.class.getDeclaredMethod("create", String.class),
				MySubClass.class.getDeclaredMethod("create", String.class),
				MySubClass.class.getDeclaredField("STATICVAL")));

		opts.setConstantFields(Arrays.asList(Constants.class.getDeclaredField("PACK_VIS_CONST2")));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, MyConstantClass.class, MySubClass.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);

	}

	public static class MyConstantClass {
		String original;
		String value;

		MyConstantClass(String base) {
			this.original = base;
			this.value = MyConstantClass.class.getSimpleName() + "_" + base;
		}

		private MyConstantClass(String original, String value) {
			this.original = original;
			this.value = value;
		}

		static MyConstantClass create(String val) {
			return new MyConstantClass(val, val + "CC");
		}

		String getOriginal() {
			return original;
		}

		String getValue() {
			return value;
		}
	}

	public static class MySubClass extends MyConstantClass {

		static final String STATICVAL;
		static final String CONSTANT_STATICVAL;
		static {
			STATICVAL = "123";
			CONSTANT_STATICVAL = "xyz";
		}

		MySubClass(String base) {
			super(base);
		}

		private MySubClass(String original, String value) {
			super(original, value);
		}

		public static MySubClass create(String val) {
			return new MySubClass(val, val + "SC");
		}

	}

	public static class Constants {
		public static final String ORIG = new MyConstantClass("abc").original;
		public static final String ORIG2 = new MyConstantClass("abc").getOriginal();

		public static final String CREATE1 = MyConstantClass.create("1").getValue();
		public static final String CREATE2 = MySubClass.create("2").getValue();

		public static final String PACK_VIS_CONST = MySubClass.STATICVAL + "x";
		public static final String PACK_VIS_CONST2 = MySubClass.CONSTANT_STATICVAL + "x";
	}

}
