package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

/**
 * Holds an object that was reconstructed based on the instructions found in a method node.
 * <p>
 * The value is reconstructed by examining the contents of the stack at a given instruction.
 */
class AsmStackReconstructedValue {
	/**
	 * Inclusive of the instructions to remove.
	 */
	protected AbstractInsnNode firstIns;
	/**
	 * Exclusive to the instruction to remove.
	 */
	protected AbstractInsnNode lastIns;
	private final Object value;

	public AsmStackReconstructedValue(AbstractInsnNode firstIns, AbstractInsnNode lastIns, Object value) {
		this.firstIns = firstIns;
		this.lastIns = lastIns;
		this.value = value;
	}

	public AbstractInsnNode getFirstIns() {
		return firstIns;
	}

	public AbstractInsnNode getLastIns() {
		return lastIns;
	}

	public Object getValue() {
		return value;
	}

	public void removeInstructions(InsnList instructions) {
		for (AbstractInsnNode it = firstIns; it != null && it != lastIns;) {
			AbstractInsnNode next = it.getNext();
			switch (it.getType()) {
				case AbstractInsnNode.LINE:
				case AbstractInsnNode.LABEL: {
					//don't remove these
					break;
				}
				default: {
					instructions.remove(it);
					break;
				}
			}
			it = next;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AsmStackReconstructedValue[");
		if (value != null) {
			builder.append("value=");
			builder.append(value);
			builder.append(", ");
		}
		if (firstIns != null) {
			builder.append("firstIns=");
			builder.append(firstIns);
			builder.append(", ");
		}
		if (lastIns != null) {
			builder.append("lastIns=");
			builder.append(lastIns);
		}
		builder.append("]");
		return builder.toString();
	}

}