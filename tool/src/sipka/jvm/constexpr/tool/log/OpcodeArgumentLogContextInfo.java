package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;

public final class OpcodeArgumentLogContextInfo extends BaseLogContextInfo {
	private final int argumentIndex;
	private final int opcode;

	public OpcodeArgumentLogContextInfo(BytecodeLocation bytecodeLocation, int argumentIndex, int opcode) {
		super(bytecodeLocation);
		this.argumentIndex = argumentIndex;
		this.opcode = opcode;
	}

	public int getArgumentIndex() {
		return argumentIndex;
	}

	public int getOpcode() {
		return opcode;
	}

	@Override
	public String getMessage() {
		switch (opcode) {
			case Opcodes.BASTORE:
			case Opcodes.SASTORE:
			case Opcodes.IASTORE:
			case Opcodes.LASTORE:
			case Opcodes.FASTORE:
			case Opcodes.DASTORE:
			case Opcodes.CASTORE:
			case Opcodes.AASTORE: {
				switch (argumentIndex) {
					case 0:
						return "When trying to reconstruct array";
					case 1:
						return "When trying to reconstruct array index";
					case 2:
						return "When trying to reconstruct array element";
					default: {
						throw new IllegalArgumentException(
								"Invalid argument index for array store opcode: " + argumentIndex);
					}
				}
			}

			case Opcodes.CHECKCAST:
			case Opcodes.I2L:
			case Opcodes.I2F:
			case Opcodes.I2D:
			case Opcodes.I2B:
			case Opcodes.I2C:
			case Opcodes.I2S:
			case Opcodes.INEG:
			case Opcodes.L2I:
			case Opcodes.L2F:
			case Opcodes.L2D:
			case Opcodes.LNEG:
			case Opcodes.F2I:
			case Opcodes.F2L:
			case Opcodes.F2D:
			case Opcodes.FNEG:
			case Opcodes.D2I:
			case Opcodes.D2L:
			case Opcodes.D2F:
			case Opcodes.DNEG:
				return "When trying to reconstruct unary operator (" + Utils.getOpcodeName(opcode) + ") argument";

			case Opcodes.IADD:
			case Opcodes.ISUB:
			case Opcodes.IMUL:
			case Opcodes.IDIV:
			case Opcodes.IREM:
			case Opcodes.ISHL:
			case Opcodes.ISHR:
			case Opcodes.IUSHR:
			case Opcodes.IAND:
			case Opcodes.IOR:
			case Opcodes.IXOR:
			case Opcodes.LADD:
			case Opcodes.LSUB:
			case Opcodes.LMUL:
			case Opcodes.LDIV:
			case Opcodes.LREM:
			case Opcodes.LSHL:
			case Opcodes.LSHR:
			case Opcodes.LUSHR:
			case Opcodes.LAND:
			case Opcodes.LOR:
			case Opcodes.LXOR:
				return "When trying to reconstruct binary operator (" + Utils.getOpcodeName(opcode) + ") "
						+ (argumentIndex == 0 ? "left" : "right") + " argument";
			default: {
				//unknown?
				return "When handling opcode: " + opcode + " (" + Utils.getOpcodeName(opcode) + ") and argument index: "
						+ argumentIndex;
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + argumentIndex;
		result = prime * result + opcode;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OpcodeArgumentLogContextInfo other = (OpcodeArgumentLogContextInfo) obj;
		if (argumentIndex != other.argumentIndex)
			return false;
		if (opcode != other.opcode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", argumentIndex=");
		builder.append(argumentIndex);
		builder.append(", opcode=");
		builder.append(opcode);
		builder.append("]");
		return builder.toString();
	}

}
