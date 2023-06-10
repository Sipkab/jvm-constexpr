package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.AsmStackInfo;
import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class InstructionReplacementLogEntry implements LogEntry {
	private BytecodeLocation bytecodeLocation;
	private AsmStackInfo replacedInfo;
	private AsmStackInfo replacementInfo;
	private Object replacementValue;

	public InstructionReplacementLogEntry(BytecodeLocation bytecodeLocation, AsmStackInfo replacedInfo,
			AsmStackInfo replacementInfo, Object replacementValue) {
		this.bytecodeLocation = bytecodeLocation;
		this.replacedInfo = replacedInfo;
		this.replacementInfo = replacementInfo;
		this.replacementValue = replacementValue;
	}

	public BytecodeLocation getBytecodeLocation() {
		return bytecodeLocation;
	}

	public AsmStackInfo getReplacedInfo() {
		return replacedInfo;
	}

	public AsmStackInfo getReplacementInfo() {
		return replacementInfo;
	}

	public Object getReplacementValue() {
		return replacementValue;
	}

	@Override
	public String getMessage() {
		String ls = System.lineSeparator();

		Type locationdescriptortype = Type.getType(bytecodeLocation.getMemberDescriptor());
		StringBuilder sb = new StringBuilder();
		if (locationdescriptortype.getSort() == Type.METHOD) {
			//inlining a method with a different instruction(s)
			sb.append("Optimized instructions in ");
			sb.append(bytecodeLocation);
			sb.append(" replaced ");
			sb.append(ls);
			Utils.appendAsmStackInfo(sb, replacedInfo, "\t");

			sb.append(ls);
			sb.append("with");
			sb.append(ls);
			if (replacedInfo.getKind() == AsmStackInfo.Kind.OPERATOR
					&& replacedInfo.getObject().equals(Opcodes.INSTANCEOF)
					&& replacementInfo.getKind() == AsmStackInfo.Kind.CONSTANT
					&& replacementInfo.getObject() instanceof Integer) {
				//special handling of instanceof result display, as the result of the operator is an int
				//but we display it as a boolean so it doesn't confuse people
				sb.append('\t');
				sb.append(((Integer) replacementInfo.getObject()).intValue() != 0);
			} else {
				Utils.appendAsmStackInfo(sb, replacementInfo, "\t");
			}
		} else {
			//the replacement happened NOT in a method, so its a replacement of a constant field value
			sb.append("Assigned constant value for ");
			Utils.appendMemberDescriptorPretty(sb, locationdescriptortype,
					Type.getObjectType(bytecodeLocation.getClassName()), bytecodeLocation.getMemberName());
			sb.append(" = ");
			sb.append(Utils.formatConstant(replacementValue));
			if (replacedInfo.getKind() == AsmStackInfo.Kind.CONSTANT) {
				//don't need to write the evaluation part if the replaced instructions is just a constant
			} else {
				sb.append(ls);
				sb.append("by evaluating");
				sb.append(ls);
				Utils.appendAsmStackInfo(sb, replacedInfo, "\t");
			}
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", replacedInfo=");
		builder.append(replacedInfo);
		builder.append(", replacementInfo=");
		builder.append(replacementInfo);
		builder.append(", replacementValue=");
		builder.append(replacementValue);
		builder.append("]");
		return builder.toString();
	}

}
