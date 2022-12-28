package sipka.jvm.constexpr.tool.options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import sipka.jvm.constexpr.tool.Utils;

final class ZipEntryToolInput<IKT> extends ToolInput<IKT> {
	private final ZipFile zf;
	private final ZipEntry entry;

	public ZipEntryToolInput(IKT inputKey, ZipFile zf, ZipEntry entry) {
		super(inputKey);
		this.zf = zf;
		this.entry = entry;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream fis = zf.getInputStream(entry)) {
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
		builder.append("zf=");
		builder.append(zf.getName());
		builder.append(", ");
		builder.append("entry=");
		builder.append(entry);
		builder.append("]");
		return builder.toString();
	}
}