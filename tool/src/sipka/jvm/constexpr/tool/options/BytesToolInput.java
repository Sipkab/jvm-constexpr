package sipka.jvm.constexpr.tool.options;

import java.io.IOException;

final class BytesToolInput<IKT> extends ToolInput<IKT> {
	private final byte[] data;

	public BytesToolInput(IKT inputKey, byte[] data) {
		super(inputKey);
		this.data = data;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return data;
	}
}