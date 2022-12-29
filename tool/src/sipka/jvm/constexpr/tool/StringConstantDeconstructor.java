package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;

/**
 * {@link ConstantDeconstructor} for the {@link String} class.
 * <p>
 * Adds a {@link LdcInsnNode} with the {@link String}.
 */
final class StringConstantDeconstructor implements ConstantDeconstructor {
	public static final StringConstantDeconstructor INSTANCE = new StringConstantDeconstructor();

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object val) {
		InsnList instructions = new InsnList();
		//cast to String just to be safe
		instructions.add(new LdcInsnNode((String) val));
		return DeconstructionResult.createConstant(instructions, val);
	}
}