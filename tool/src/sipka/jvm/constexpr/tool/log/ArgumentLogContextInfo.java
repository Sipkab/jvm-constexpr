package sipka.jvm.constexpr.tool.log;

public final class ArgumentLogContextInfo extends BaseLogContextInfo {
	private final int argumentIndex;

	public ArgumentLogContextInfo(BytecodeLocation bytecodeLocation, int argumentIndex) {
		super(bytecodeLocation);
		this.argumentIndex = argumentIndex;
	}

	public int getArgumentIndex() {
		return argumentIndex;
	}

	@Override
	public String getMessage() {
		return "When trying to reconstruct argument at index: " + argumentIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + argumentIndex;
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
		ArgumentLogContextInfo other = (ArgumentLogContextInfo) obj;
		if (argumentIndex != other.argumentIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", argumentIndex=");
		builder.append(argumentIndex);
		builder.append("]");
		return builder.toString();
	}

}
