package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class NotReconstructableConstantReconstructor implements ConstantReconstructor {
	public static final NotReconstructableConstantReconstructor INSTANCE = new NotReconstructableConstantReconstructor();

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		return null;
	}

}
