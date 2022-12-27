package testing.sipka.jvm.constexpr;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that static final fields intialized in the static intializer using conditional statements are properly set, but
 * only if the initialization is unequivocal.
 */
@SakerTest
public class ConditionalConstantInitializerTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		NavigableMap<String, FieldNode> fields = TestUtils.getFields(classnode);

		for (Field f : Constants.class.getDeclaredFields()) {
			String fieldname = f.getName();
			FieldNode fnode = fields.get(fieldname);
			Object asmval = TestUtils.getAsmFieldNodeValue(fnode, f.getType());
			switch (fieldname) {
				case "diffIfInitialized":
				case "diffSwitchInitialized": {
					//these cannot be set
					assertNull(asmval, fieldname);
					break;
				}
				default: {
					assertEquals(asmval, f.get(null), fieldname);
					break;
				}
			}
		}
	}

	public static class Constants {
		public static final String diffIfInitialized;
		public static final String sameIfInitialized;

		public static final String diffSwitchInitialized;
		public static final String sameSwitchInitialized;

		static {
			if (Constants.class.hashCode() % 2 == 0) {
				diffIfInitialized = "hc0";
				sameIfInitialized = "sameifstr";
			} else {
				diffIfInitialized = "hc1";
				sameIfInitialized = "sameifstr";
			}
			switch (Constants.class.hashCode() % 2) {
				case 0: {
					diffSwitchInitialized = "ds0";
					sameSwitchInitialized = "sameswstr";
					break;
				}
				case 1: {
					diffSwitchInitialized = "ds1";
					sameSwitchInitialized = "sameswstr";
					break;
				}
				default: {
					throw new AssertionError();
				}
			}
		}
	}
}
