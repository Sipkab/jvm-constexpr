package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.options.ToolInput;

public final class BytecodeLocation {
	private transient ToolInput<?> input;

	private String className;
	private String methodName;
	private String methodDescriptor;
	private int line;

	public BytecodeLocation(ToolInput<?> input, String className, String methodName, String methodDescriptor,
			int line) {
		this.input = input;
		this.className = className;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
		this.line = line;
	}

	public ToolInput<?> getInput() {
		return input;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getLine() {
		return line;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + line;
		result = prime * result + ((methodDescriptor == null) ? 0 : methodDescriptor.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
		StringBuilder sb = new StringBuilder();
		sb.append(className);
		sb.append(".");
		sb.append(methodName);
		sb.append(methodDescriptor);
		if (line >= 0) {
			sb.append(" (line ");
			sb.append(line);
			sb.append(')');
		}
		return sb.toString();
	}
}
