package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.thirdparty.saker.util.ReflectUtils;
import sipka.jvm.constexpr.tool.ConstantExpressionInliner;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassWriter;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.VarInsnNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that the correct fields are accessed when multiple fields are declared in the class with the same name, but
 * different descriptors.
 */
@SakerTest
public class SameNameFieldInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		ClassLoader testcl;
		{
			ClassReader cr = new ClassReader(
					ReflectUtils.getClassBytesUsingClassLoader(MyConstantClass.class).copyOptionally());
			ClassNode cn = new ClassNode(ConstantExpressionInliner.ASM_API);
			cn.fields.add(new FieldNode(0, "value", Type.getDescriptor(int.class), null, null));
			//add an int getValue too, but this test case doesn't focus on that
			MethodNode igetvalmethodnode = new MethodNode(0, "getValue", Type.getMethodDescriptor(Type.INT_TYPE), null,
					null);
			igetvalmethodnode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
			igetvalmethodnode.instructions.add(new FieldInsnNode(Opcodes.GETFIELD,
					Type.getInternalName(MyConstantClass.class), "value", Type.INT_TYPE.getDescriptor()));
			igetvalmethodnode.instructions.add(new InsnNode(Opcodes.IRETURN));
			cn.methods.add(igetvalmethodnode);

			cr.accept(cn, ClassReader.EXPAND_FRAMES);
			ClassWriter cw = new ClassWriter(cr, 0);

			cn.accept(cw);

			testcl = TestUtils.createClassLoader(cn);
		}

		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		opts.setConstantTypes(Arrays.asList(Class.forName(MyConstantClass.class.getName(), false, testcl)));
		opts.setConstantReconstructors(TestUtils.allowAllMembers(Arrays.asList()));
		opts.setConstantFields(Arrays.asList());

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false, TestUtils.createClassLoader(classnode));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);
	}

	public static class MyConstantClass {
		String value;

		MyConstantClass(String sval) {
			this.value = sval;
		}

		MyConstantClass(int ival) {
		}

		public String getValue() {
			return value;
		}

	}

	public static class Constants {
		public static final String C1 = new MyConstantClass("abc").getValue();
		public static final String C2 = new MyConstantClass(456).getValue();
		public static final String C3 = new MyConstantClass("xyz").value;
	}

}
