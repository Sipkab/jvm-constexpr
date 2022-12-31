package sipka.jvm.constexpr.tool.log;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class NewArrayLogContextInfo extends BaseLogContextInfo {
	private final String arrayComponentType;
	private final int size;

	public NewArrayLogContextInfo(BytecodeLocation bytecodeLocation, String arrayComponentType, int size) {
		super(bytecodeLocation);
		this.arrayComponentType = arrayComponentType;
		this.size = size;
	}

	public String getArrayComponentType() {
		return arrayComponentType;
	}

	//-1 if failed to get the size
	public int getSize() {
		return size;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("When trying to create new array: ");
		sb.append(Type.getObjectType(arrayComponentType).getClassName());
		if (size < 0) {
			sb.append("[]");
		} else {
			sb.append('[');
			sb.append(size);
			sb.append(']');
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((arrayComponentType == null) ? 0 : arrayComponentType.hashCode());
		result = prime * result + size;
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
		NewArrayLogContextInfo other = (NewArrayLogContextInfo) obj;
		if (arrayComponentType == null) {
			if (other.arrayComponentType != null)
				return false;
		} else if (!arrayComponentType.equals(other.arrayComponentType))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[bytecodeLocation=");
		builder.append(bytecodeLocation);
		builder.append(", arrayComponentType=");
		builder.append(arrayComponentType);
		builder.append(", size=");
		builder.append(size);
		builder.append("]");
		return builder.toString();
	}

}
