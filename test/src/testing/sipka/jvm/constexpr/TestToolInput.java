package testing.sipka.jvm.constexpr;

import java.io.IOException;

import sipka.jvm.constexpr.tool.options.ToolInput;

public class TestToolInput extends ToolInput<Object> {
	private byte[] bytes;

	public TestToolInput(Object inputKey, byte[] bytes) {
		super(inputKey);
		this.bytes = bytes;
	}

	public TestToolInput(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return bytes;
	}

}
