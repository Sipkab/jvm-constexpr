package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class MethodNotFoundLogContextInfo extends BaseLogContextInfo {
	private final String className;
	private final String methodName;
	private final String methodDescriptor;

	public MethodNotFoundLogContextInfo(BytecodeLocation bytecodeLocation, String className, String methodName,
			String methodDescriptor) {
		super(bytecodeLocation);
		this.className = className;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		if (Utils.CONSTRUCTOR_METHOD_NAME.equals(methodName)) {
			sb.append("Constructor not found: ");
		} else {
			sb.append("Method not found: ");
		}
		Utils.appendMemberDescriptorPretty(sb, Type.getType(methodDescriptor), Type.getObjectType(className),
				methodName);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodDescriptor == null) ? 0 : methodDescriptor.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
		MethodNotFoundLogContextInfo other = (MethodNotFoundLogContextInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodDescriptor == null) {
			if (other.methodDescriptor != null)
				return false;
		} else if (!methodDescriptor.equals(other.methodDescriptor))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
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
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", methodDescriptor=");
		builder.append(methodDescriptor);
		builder.append("]");
		return builder.toString();
	}

}
