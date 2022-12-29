package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class BytecodeLocation {
	private transient ToolInput<?> input;

	private String className;
	private String memberName;
	private String memberDescriptor;
	private int line;

	public BytecodeLocation(ToolInput<?> input, String className, String memberName, String memberDescriptor,
			int line) {
		this.input = input;
		this.className = className;
		this.memberName = memberName;
		this.memberDescriptor = memberDescriptor;
		this.line = line;
	}

	public ToolInput<?> getInput() {
		return input;
	}

	public String getClassName() {
		return className;
	}

	public String getMemberName() {
		return memberName;
	}

	public String getMemberDescriptor() {
		return memberDescriptor;
	}

	public int getLine() {
		return line;
	}

	public int compareLocation(BytecodeLocation r) {
		int cmp = this.className.compareTo(r.className);
		if (cmp != 0) {
			return cmp;
		}
		cmp = this.memberName.compareTo(r.memberName);
		if (cmp != 0) {
			return cmp;
		}
		cmp = this.memberDescriptor.compareTo(r.memberDescriptor);
		if (cmp != 0) {
			return cmp;
		}
		return Integer.compare(this.line, r.line);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + line;
		result = prime * result + ((memberDescriptor == null) ? 0 : memberDescriptor.hashCode());
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BytecodeLocation other = (BytecodeLocation) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (line != other.line)
			return false;
		if (memberDescriptor == null) {
			if (other.memberDescriptor != null)
				return false;
		} else if (!memberDescriptor.equals(other.memberDescriptor))
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Utils.appendMemberDescriptorPretty(sb, Type.getType(memberDescriptor), Type.getObjectType(className),
				memberName);
		if (line >= 0) {
			sb.append(" (line ");
			sb.append(line);
			sb.append(')');
		}
		return sb.toString();
	}
}
