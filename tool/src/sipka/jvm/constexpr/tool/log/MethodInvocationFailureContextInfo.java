package sipka.jvm.constexpr.tool.log;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class MethodInvocationFailureContextInfo extends BaseLogContextInfo {
	private final String className;
	private final String methodName;
	private final String methodDescriptor;
	private final Object instance;
	private final Object[] arguments;

	public MethodInvocationFailureContextInfo(BytecodeLocation bytecodeLocation, String className, String methodName,
			String methodDescriptor, Object instance, Object[] arguments) {
		super(bytecodeLocation);
		this.className = className;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
		this.instance = instance;
		this.arguments = arguments;
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

	public Object getInstance() {
		return instance;
	}

	public Object[] getArguments() {
		return arguments;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		if (Utils.CONSTRUCTOR_METHOD_NAME.equals(methodName)) {
			sb.append("Failed to create new instance ");
		} else {
			sb.append("Failed to invoke method: ");
		}

		Utils.appendMemberDescriptorPretty(sb, Type.getType(methodDescriptor), Type.getObjectType(className),
				methodName);
		String ls = System.lineSeparator();
		if (instance != null) {
			sb.append(ls);
			sb.append("\t\ton instance: ");
			sb.append(instance);
		}
		for (int i = 0; i < arguments.length; i++) {
			sb.append(ls);
			sb.append("\t\targument[");
			sb.append(i);
			sb.append("]");
			Object arg = arguments[i];
			if (arg == null) {
				sb.append(": null");
			} else {
				sb.append(' ');
				sb.append(arg.getClass().getName());
				sb.append(": ");
				sb.append(arg);
			}
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.deepHashCode(arguments);
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
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
		MethodInvocationFailureContextInfo other = (MethodInvocationFailureContextInfo) obj;
		if (!Arrays.deepEquals(arguments, other.arguments))
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
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
		builder.append(", instance=");
		builder.append(instance);
		builder.append(", arguments=");
		builder.append(Arrays.toString(arguments));
		builder.append("]");
		return builder.toString();
	}

}
