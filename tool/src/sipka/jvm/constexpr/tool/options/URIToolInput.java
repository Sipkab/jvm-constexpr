package sipka.jvm.constexpr.tool.options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import sipka.jvm.constexpr.tool.Utils;

final class URIToolInput<IKT> extends ToolInput<IKT> {
	private final URI uri;

	public URIToolInput(IKT inputKey, URI uri) {
		super(inputKey);
		this.uri = uri;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream fis = uri.toURL().openStream()) {
			Utils.copyStream(fis, baos);
		}
		return baos.toByteArray();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (inputKey != null) {
			builder.append("inputKey=");
			builder.append(inputKey);
			builder.append(", ");
		}
		builder.append("url=");
		builder.append(uri);
		builder.append("]");
		return builder.toString();
	}
}