package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class FieldAccessFailureContextInfo extends BaseLogContextInfo {
	private final String className;
	private final String fieldName;
	private final String fieldDescriptor;
	private final Object instance;

	public FieldAccessFailureContextInfo(BytecodeLocation bytecodeLocation, String className, String methodName,
			String methodDescriptor, Object instance) {
		super(bytecodeLocation);
		this.className = className;
		this.fieldName = methodName;
		this.fieldDescriptor = methodDescriptor;
		this.instance = instance;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return fieldName;
	}

	public String getMethodDescriptor() {
		return fieldDescriptor;
	}

	public Object getInstance() {
		return instance;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Failed to access field: ");
		Utils.appendMemberDescriptorPretty(sb, Type.getType(fieldDescriptor), Type.getObjectType(className), fieldName);
		if (instance != null) {
			sb.append(System.lineSeparator());
			sb.append("\t\ton instance: ");
			sb.append(instance);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((fieldDescriptor == null) ? 0 : fieldDescriptor.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
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
		FieldAccessFailureContextInfo other = (FieldAccessFailureContextInfo) obj;
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
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
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
		builder.append(", instance=");
		builder.append(instance);
		builder.append("]");
		return builder.toString();
	}

}
