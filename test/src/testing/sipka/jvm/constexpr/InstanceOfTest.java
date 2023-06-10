package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class InstanceOfTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		opts.setConstantReconstructors(TestUtils.allowAllMembers(
				Arrays.asList(Constants.class.getMethod("getValue"), Constants.class.getMethod("getArrayValue"))));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		assertNull(TestUtils.getClInitMethod(classnode));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final boolean INSTANCEOF = getValue() instanceof String;
		public static final boolean INSTANCEOF2 = getValue() instanceof Integer;
		public static final boolean INSTANCEOF3 = getArrayValue() instanceof Object[];
		public static final boolean INSTANCEOF4 = getArrayValue() instanceof CharSequence[];
		public static final boolean INSTANCEOF5 = getArrayValue() instanceof CharSequence;

		public static final String strINSTANCEOF = String.valueOf(INSTANCEOF);
		public static final String strINSTANCEO2 = String.valueOf(INSTANCEOF2);

		public static Object getValue() {
			return "abc";
		}

		public static Object getArrayValue() {
			return new String[] { "abc", "edf" };
		}
	}

}
