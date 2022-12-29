package sipka.jvm.constexpr.tool;

import java.lang.reflect.Method;

public final class MethodDeconstructionDataAccessor implements DeconstructionDataAccessor {
	private final Method method;
	private final Class<?> receiverType;

	public MethodDeconstructionDataAccessor(Method method) {
		this(method, method.getReturnType());
	}

	public MethodDeconstructionDataAccessor(Method method, Class<?> receiverType) {
		this.method = method;
		this.receiverType = receiverType;
	}

	@Override
	public DeconstructedData getData(Object value) throws Exception {
		return new DeconstructedData(method.invoke(value), receiverType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((receiverType == null) ? 0 : receiverType.hashCode());
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
		MethodDeconstructionDataAccessor other = (MethodDeconstructionDataAccessor) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (receiverType == null) {
			if (other.receiverType != null)
				return false;
		} else if (!receiverType.equals(other.receiverType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[method=");
		builder.append(method);
		builder.append(", receiverType=");
		builder.append(receiverType);
		builder.append("]");
		return builder.toString();
	}

}