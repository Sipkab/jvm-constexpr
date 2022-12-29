package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class FieldNotFoundLogContextInfo extends BaseLogContextInfo {
	private final String className;
	private final String fieldName;
	private final String fieldDescriptor;

	public FieldNotFoundLogContextInfo(BytecodeLocation bytecodeLocation, String className, String fieldName,
			String fieldDescriptor) {
		super(bytecodeLocation);
		this.className = className;
		this.fieldName = fieldName;
		this.fieldDescriptor = fieldDescriptor;
	}

	public String getClassName() {
		return className;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getFieldDescriptor() {
		return fieldDescriptor;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field not found: ");
		Utils.appendMemberDescriptorPretty(sb, Type.getType(fieldDescriptor), Type.getObjectType(className), fieldName);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((fieldDescriptor == null) ? 0 : fieldDescriptor.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		FieldNotFoundLogContextInfo other = (FieldNotFoundLogContextInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
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
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", className=");
		builder.append(className);
		builder.append(", fieldName=");
		builder.append(fieldName);
		builder.append(", fieldDescriptor=");
		builder.append(fieldDescriptor);
		builder.append("]");
		return builder.toString();
	}

}
