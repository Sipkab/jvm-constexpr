package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class MultipleInitializationPathLogEntry implements LogEntry {

	private final String classInternalName;
	private final String fieldName;
	private final String fieldDescriptor;

	public MultipleInitializationPathLogEntry(String classInternalName, String fieldName, String fieldDescriptor) {
		this.classInternalName = classInternalName;
		this.fieldName = fieldName;
		this.fieldDescriptor = fieldDescriptor;
	}

	public String getClassInternalName() {
		return classInternalName;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field cannot be inlined because it has multiple initialization paths: ");
		Utils.appendMemberDescriptorPretty(sb, Type.getType(fieldDescriptor), Type.getObjectType(classInternalName),
				fieldName);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classInternalName == null) ? 0 : classInternalName.hashCode());
		result = prime * result + ((fieldDescriptor == null) ? 0 : fieldDescriptor.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		MultipleInitializationPathLogEntry other = (MultipleInitializationPathLogEntry) obj;
		if (classInternalName == null) {
			if (other.classInternalName != null)
				return false;
		} else if (!classInternalName.equals(other.classInternalName))
			return false;
		if (fieldDescriptor == null) {
			if (other.fieldDescriptor != null)
				return false;
		} else if (!fieldDescriptor.equals(other.fieldDescriptor))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[classInternalName=");
		builder.append(classInternalName);
		builder.append(", fieldName=");
		builder.append(fieldName);
		builder.append(", fieldDescriptor=");
		builder.append(fieldDescriptor);
		builder.append("]");
		return builder.toString();
	}

}
