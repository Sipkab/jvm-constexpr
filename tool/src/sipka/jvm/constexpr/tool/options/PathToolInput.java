package sipka.jvm.constexpr.tool.options;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import sipka.jvm.constexpr.tool.Utils;

final class PathToolInput<IKT> extends ToolInput<IKT> {
	private final Path classFile;

	public PathToolInput(IKT inputKey, Path classfile) {
		super(inputKey);
		this.classFile = classfile;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream fis = Files.newInputStream(classFile)) {
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
		builder.append("classFile=");
		builder.append(classFile);
		builder.append("]");
		return builder.toString();
	}
}