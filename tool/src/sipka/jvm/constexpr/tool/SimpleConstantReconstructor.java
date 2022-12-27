package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

final class SimpleConstantReconstructor implements ConstantReconstructor {
	private final Object val;

	public SimpleConstantReconstructor(Object val) {
		this.val = val;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		return new AsmStackReconstructedValue(ins, ins.getNext(), val);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[val=");
		builder.append(val);
		builder.append("]");
		return builder.toString();
	}

}