package sipka.jvm.constexpr.tool;

public final class DeconstructedData {
	private final Object data;
	private final Class<?> receiverType;

	public DeconstructedData(Object data, Class<?> receiverType) {
		this.data = data;
		this.receiverType = receiverType;
	}

	public Object getData() {
		return data;
	}

	public Class<?> getReceiverType() {
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
