package testing.sipka.jvm.constexpr;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests inlining when the field type, or value type is not primitive/String
 */
@SakerTest
public class NonConstantTypeInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final String STR = "str";
		public static final Object OBJSTR = "str";

		public static final String STR_OBJ = STR + OBJSTR;

		//check that the constant value of UUIDVAL is properly propagated to other constants that reference it
		public static final UUID UUIDVAL = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_8));
		public static final String UUIDSTR = UUIDVAL.toString();
		public static final long UUIDMSB = UUIDVAL.getMostSignificantBits();
	}
}
