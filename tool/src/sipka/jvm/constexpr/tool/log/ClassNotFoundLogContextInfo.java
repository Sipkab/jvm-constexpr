package sipka.jvm.constexpr.tool.log;

public class ClassNotFoundLogContextInfo extends BaseLogContextInfo {
	private final String className;

	public ClassNotFoundLogContextInfo(BytecodeLocation bytecodeLocation, String className) {
		super(bytecodeLocation);
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String getMessage() {
		return "Class not found with name: " + className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((className == null) ? 0 : className.hashCode());
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
		ClassNotFoundLogContextInfo other = (ClassNotFoundLogContextInfo) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
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
		builder.append("]");
		return builder.toString();
	}

}
