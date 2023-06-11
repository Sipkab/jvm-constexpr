package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link Class} class.
 * <p>
 * Adds a {@link LdcInsnNode} with the {@link Type}.
 */
final class ClassConstantDeconstructor implements ConstantDeconstructor {
	public static final ClassConstantDeconstructor INSTANCE = new ClassConstantDeconstructor();

	public ClassConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object val) {
		InsnList instructions = new InsnList();
		instructions.add(new LdcInsnNode(Type.getType((Class<?>) val)));
		return DeconstructionResult.createConstant(instructions, val);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}