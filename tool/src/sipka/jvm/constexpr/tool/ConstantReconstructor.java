package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Interface for reconstructing a value based on the method instructions at a given point in a method.
 */
interface ConstantReconstructor {
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins);
}