package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import javax.lang.model.SourceVersion;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that forward compatibility with non-existent enum fields are correct.
 * <p>
 * {@link SourceVersion} is used for this, as a new field gets introduced to that in each release.
 */
@SakerTest
public class EnumFieldForwardCompatibilityInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, Constants.class, "LATEST", "LATESTSUPPORTED");
		}
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Failer.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			assertNull(TestUtils.getFields(classnode).get("T3").value, "T3");
		}
	}

	public static class Constants {
		public static final String T1 = SourceVersion.RELEASE_0.name();
		public static final String T2;
		public static final String LATEST = SourceVersion.latest().name();
		public static final String LATESTSUPPORTED = SourceVersion.latestSupported().name();
		static {
			T2 = SourceVersion.valueOf("RELEASE_0").name();
		}
	}

	public static class Failer {
		//this value shouldn't get inlined, as the enum doesn't exist
		//the IllegalArgumentException throwing behaviour should be kept
		public static final String T3;
		static {
			T3 = SourceVersion.valueOf("RELEASE_99999").name();
		}
	}
}
