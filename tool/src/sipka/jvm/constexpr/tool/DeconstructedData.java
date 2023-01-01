package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class DeconstructedData {
	private final Object data;
	private final Type receiverType;

	public DeconstructedData(Object data, Type receiverType) {
		this.data = data;
		this.receiverType = receiverType;
	}

	public Object getData() {
		return data;
	}

	public Type getReceiverType() {
		return receiverType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[data=");
		builder.append(data);
		builder.append(", receiverType=");
		builder.append(receiverType);
		builder.append("]");
		return builder.toString();
	}

}
