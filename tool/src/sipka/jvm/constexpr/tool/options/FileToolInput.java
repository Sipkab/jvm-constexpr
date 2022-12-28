package sipka.jvm.constexpr.tool.options;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import sipka.jvm.constexpr.tool.Utils;

final class FileToolInput<IKT> extends ToolInput<IKT> {
	private final File classFile;

	public FileToolInput(IKT inputKey, File classfile) {
		super(inputKey);
		this.classFile = classfile;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (InputStream fis = new FileInputStream(classFile)) {
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