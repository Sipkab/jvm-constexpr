package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the fields are correctly deconstructed if they are shadowed by a superclass.
 */
@SakerTest
public class ShadowFieldInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		opts.setConstantTypes(Arrays.asList(MySuperClass.class, MySubClass.class));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, MySuperClass.class, MySubClass.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);
	}

	public static class MySuperClass {
		String original;
		String value;

		public MySuperClass(String base) {
			this.original = base;
			this.value = MySuperClass.class.getSimpleName() + "_" + base;
		}
	}

	public static class MySubClass extends MySuperClass {
		String value;

		public MySubClass(String base) {
			super(base);
			this.value = MySubClass.class.getSimpleName() + "_" + base;
		}

		public String getValue() {
			return value;
		}
	}

	public static class Constants {
		public static final String ORIG = new MySubClass("abc").original;
		public static final String VSUB = new MySubClass("abc").value;
		public static final String VSUPER = ((MySuperClass) new MySubClass("abc")).value;
	}

}
