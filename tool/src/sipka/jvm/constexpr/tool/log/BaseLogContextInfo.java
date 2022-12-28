package sipka.jvm.constexpr.tool.log;

abstract class BaseLogContextInfo implements LogContextInfo {
	protected final BytecodeLocation bytecodeLocation;

	public BaseLogContextInfo(BytecodeLocation bytecodeLocation) {
		this.bytecodeLocation = bytecodeLocation;
	}

	@Override
	public BytecodeLocation getBytecodeLocation() {
		return bytecodeLocation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bytecodeLocation == null) ? 0 : bytecodeLocation.hashCode());
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
		BaseLogContextInfo other = (BaseLogContextInfo) obj;
		if (bytecodeLocation == null) {
			if (other.bytecodeLocation != null)
				return false;
		} else if (!bytecodeLocation.equals(other.bytecodeLocation))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append("]");
		return builder.toString();
	}

}
