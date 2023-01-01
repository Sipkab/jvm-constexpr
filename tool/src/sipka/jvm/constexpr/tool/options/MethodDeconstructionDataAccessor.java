package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.DeconstructedData;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class MethodDeconstructionDataAccessor implements DeconstructionDataAccessor {
	private final Method method;
	private final Type receiverType;

	public MethodDeconstructionDataAccessor(Method method) {
		this(method, Type.getType(method.getReturnType()));
	}

	public MethodDeconstructionDataAccessor(Method method, Type receiverType) {
		this.method = method;
		this.receiverType = receiverType;
	}

	public Method getMethod() {
		return method;
	}

	public Type getReceiverType() {
		return receiverType;
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