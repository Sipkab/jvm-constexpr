package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class ConfigClassMemberInaccessibleLogEntry implements LogEntry {
	private final String className;
	private final String memberName;
	private final String memberDescriptor;

	private final transient Throwable exception;

	public ConfigClassMemberInaccessibleLogEntry(String className, String memberName, String memberDescriptor,
			Throwable exception) {
		this.className = className;
		this.memberName = memberName;
		this.memberDescriptor = memberDescriptor;
		this.exception = exception;
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

	//may be null
	public Throwable getException() {
		return exception;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		if (memberName == null) {
			sb.append("Class ");
			if (exception instanceof ClassNotFoundException) {
				sb.append("not found");
			} else {
				sb.append("inaccessible");
			}
			sb.append(" in current JVM: ");
			sb.append(Type.getObjectType(className).getClassName());
		} else {
			Type memberdesc = Type.getType(memberDescriptor);
			if (memberdesc.getSort() == Type.METHOD) {
				if (Utils.CONSTRUCTOR_METHOD_NAME.equals(memberName)) {
					sb.append("Constructor ");
				} else {
					sb.append("Method ");
				}
			} else {
				sb.append("Field ");
			}
			if (exception instanceof NoSuchMethodException || exception instanceof NoSuchMethodException) {
				sb.append("not found");
			} else {
				sb.append("inaccessible");
			}
			sb.append(" in current JVM: ");
			Utils.appendMemberDescriptorPretty(sb, memberdesc, Type.getObjectType(className), memberName);
		}
		Throwable rc = exception;
		if (rc != null) {
			sb.append(System.lineSeparator());
			sb.append("Caused by: ");
			Utils.appendThrowableStackTrace(sb, rc);
		}
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
		ConfigClassMemberInaccessibleLogEntry other = (ConfigClassMemberInaccessibleLogEntry) obj;
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
		builder.append("[className=");
		builder.append(className);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", memberDescriptor=");
		builder.append(memberDescriptor);
		builder.append(", exception=");
		builder.append(exception);
		builder.append("]");
		return builder.toString();
	}

}
