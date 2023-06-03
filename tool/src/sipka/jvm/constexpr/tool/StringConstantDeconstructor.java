package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link String} class.
 * <p>
 * Adds a {@link LdcInsnNode} with the {@link String}.
 */
final class StringConstantDeconstructor implements ConstantDeconstructor {
	public static final StringConstantDeconstructor INSTANCE = new StringConstantDeconstructor();

	public StringConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object val) {
		InsnList instructions = new InsnList();
		//cast to String just to be safe
		instructions.add(new LdcInsnNode((String) val));
		return DeconstructionResult.createConstant(instructions, val);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}