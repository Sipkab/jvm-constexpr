package testing.sipka.jvm.constexpr;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.NavigableMap;
import java.util.UUID;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests inlining the {@link UUID} instances.
 */
@SakerTest
public class UUIDInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		public static final String uuidStr;
		public static final String uuidObjectStr;
		public static final long uuidmsb1;
		public static final long uuidlsb1;

		public static final long uuidmsb2;
		public static final long uuidlsb2;

		public static final long uuidmsb3;
		public static final long uuidlsb3;

		public static final long uuidmsb4;
		public static final long uuidlsb4;

		public static final long uuidmsb5;
		public static final long uuidlsb5;

		static {
			uuidStr = new UUID(999, 777).toString();
			uuidObjectStr = ((Object) new UUID(888, 666)).toString();
			uuidmsb1 = new UUID(123, 456).getMostSignificantBits();
			uuidlsb1 = new UUID(123, 456).getLeastSignificantBits();

			uuidmsb2 = UUID.fromString("f3d07547-bb76-4d25-9c23-d1ce6b6f4ab5").getMostSignificantBits();
			uuidlsb2 = UUID.fromString("f3d07547-bb76-4d25-9c23-d1ce6b6f4ab5").getLeastSignificantBits();

			uuidmsb3 = UUID.nameUUIDFromBytes(new byte[] { 1, 2, 3 }).getMostSignificantBits();
			uuidlsb3 = UUID.nameUUIDFromBytes(new byte[] { 1, 2, 3 }).getLeastSignificantBits();

			uuidmsb4 = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_8)).getMostSignificantBits();
			uuidlsb4 = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_8)).getLeastSignificantBits();

			uuidmsb5 = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_16)).getMostSignificantBits();
			uuidlsb5 = UUID.nameUUIDFromBytes("test".getBytes(StandardCharsets.UTF_16)).getLeastSignificantBits();
		}

	}
}
