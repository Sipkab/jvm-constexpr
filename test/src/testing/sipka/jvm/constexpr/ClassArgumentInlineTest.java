package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import javax.lang.model.SourceVersion;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the {@link Class} types are properly loaded for constant method calls.
 */
@SakerTest
public class ClassArgumentInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		//set the getter method as a constant reconstructor, so it can be called by the inliner
		opts.setConstantReconstructors(Arrays.asList(Constants.class.getMethod("getTypeName", Class.class)));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final String TYPENAME1 = getTypeName(Object.class);
		public static final String TYPENAME2 = getTypeName(SourceVersion.RELEASE_0.getDeclaringClass());

		public static String getTypeName(Class<?> c) {
			return c.getTypeName();
		}
	}

}
