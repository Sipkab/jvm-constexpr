package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that instructions that are not method calls are inlined as well.
 */
@SakerTest
public class InstructionInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
			assertNull(TestUtils.getClInitMethod(classnode), "clinit method");

			//just a few of the instructions to check that they are indeed optimized in the method body
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "iadd", "()I"), Opcodes.IADD);
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "ineg", "()I"), Opcodes.INEG);
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "arraylength", "()I"),
					Opcodes.ARRAYLENGTH);
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "dneg", "()D"), Opcodes.DNEG);
			TestUtils.assertNoOpcodeInMethod(TestUtils.getMethodNode(classnode, "i2l", "()J"), Opcodes.I2L);
		}
	}

	public static class Constants {
		public static final int I123 = Integer.parseInt("123");
		public static final int I456 = Integer.parseInt("456");
		public static final double D2 = Integer.parseInt("2");

		public static int iadd() {
			return I123 + I456;
		}

		public static long i2l() {
			return I456;
		}

		public static int ineg() {
			return -I456;
		}

		public static double dneg() {
			return -D2;
		}

		public static int arraylength() {
			return new int[3].length;
		}
	}

}
