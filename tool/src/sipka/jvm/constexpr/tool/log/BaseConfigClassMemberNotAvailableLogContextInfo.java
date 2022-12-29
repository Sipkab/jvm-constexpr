package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class BaseConfigClassMemberNotAvailableLogContextInfo extends BaseLogContextInfo {
	private final String className;
	private final String memberName;
	private final String memberDescriptor;

	public BaseConfigClassMemberNotAvailableLogContextInfo(BytecodeLocation bytecodeLocation, String className,
			String memberName, String memberDescriptor) {
		super(bytecodeLocation);
		this.className = className;
		this.memberName = memberName;
		this.memberDescriptor = memberDescriptor;
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

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		Type memberdesc = Type.getType(memberDescriptor);
		if (memberdesc.getSort() == Type.METHOD) {
			if (Utils.CONSTRUCTOR_METHOD_NAME.equals(memberName)) {
				sb.append("Constructor not found in current JVM: ");
			} else {
				sb.append("Method not found in current JVM: ");
			}
		} else {
			sb.append("Field not found in current JVM: ");
		}
		Utils.appendMemberDescriptorPretty(sb, memberdesc, Type.getObjectType(className), memberName);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((memberDescriptor == null) ? 0 : memberDescriptor.hashCode());
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
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
		BaseConfigClassMemberNotAvailableLogContextInfo other = (BaseConfigClassMemberNotAvailableLogContextInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
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
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", className=");
		builder.append(className);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", memberDescriptor=");
		builder.append(memberDescriptor);
		builder.append("]");
		return builder.toString();
	}

}
