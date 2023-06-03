package testing.sipka.jvm.constexpr;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

import saker.build.thirdparty.saker.util.ReflectUtils;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests inline method configuration.
 */
@SakerTest
public class InlineMethodTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		List<Member> inlinemethods = new ArrayList<>();
		inlinemethods.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "computer"));
		inlinemethods.add(ReflectUtils.getDeclaredMethodAssert(System.class, "currentTimeMillis"));
		inlinemethods.add(ReflectUtils.getDeclaredMethodAssert(System.class, "lineSeparator"));
		opts.setConstantReconstructors(TestUtils.allowAllMembers(inlinemethods));
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();

		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final long computed = computer();
		public static final long millis = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(365);

		public static final String LS = System.lineSeparator();

		public static final String CONCAT = System.lineSeparator() + computer();
		public static final String CONCAT2 = LS + computer();
		public static final String CONCAT3 = LS + computed;

		private static long computer() {
			return Constants.class.getName().hashCode();
		}
	}
}
