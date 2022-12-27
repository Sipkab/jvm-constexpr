package sipka.jvm.constexpr.tool.options;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import sipka.jvm.constexpr.tool.Utils;

/**
 * Represents an input class for the constant inliner tool.
 * <p>
 * The class should be subclassed so that it returns a bytes of the given input in an appropriate manner.
 * <p>
 * Intancase also holds an user defined {@linkplain #getInputKey() input key} that helps identifying the input when
 * later passed back to the caller.
 * <p>
 * Clients can also use the static factory methods to create new instances.
 * 
 * @param <IKeyType>
 *            The input key type.
 */
public abstract class ToolInput<IKeyType> {

	protected final IKeyType inputKey;

	/**
	 * Creates a new instance with the given input key.
	 * 
	 * @param inputKey
	 *            The key, may be <code>null</code>.
	 */
	public ToolInput(IKeyType inputKey) {
		this.inputKey = inputKey;
	}

	/**
	 * Creates a new instance with <code>null</code> input key.
	 */
	public ToolInput() {
		this(null);
	}

	/**
	 * Gets the input key of the instance.
	 * <p>
	 * An input key is a caller provided arbitrary object that helps uniquely identifying it. It may be any object, or
	 * <code>null</code>.
	 * 
	 * @return The input key.
	 */
	public final Object getInputKey() {
		return inputKey;
	}

	public abstract byte[] getBytes() throws IOException;

	public static <IKT> ToolInput<IKT> createWithBytes(byte[] data) {
		return createWithBytes(null, data);
	}

	public static <IKT> ToolInput<IKT> createWithBytes(IKT inputkey, byte[] data) {
		return new BytesToolInput<IKT>(inputkey, data);
	}

	public static <IKT> ToolInput<IKT> createForFile(File classfile) {
		return createForFile(null, classfile);
	}

	public static <IKT> ToolInput<IKT> createForFile(IKT inputkey, File classfile) {
		return new FileToolInput<IKT>(inputkey, classfile);
	}

	public static <IKT> ToolInput<IKT> createForPath(Path classfile) {
		return createForPath(null, classfile);
	}

	public static <IKT> ToolInput<IKT> createForPath(IKT inputkey, Path classfile) {
		return new PathToolInput<IKT>(inputkey, classfile);
	}

	public static <IKT> ToolInput<IKT> createForURL(URL url) {
		return createForURL(null, url);
	}

	public static <IKT> ToolInput<IKT> createForURL(IKT inputkey, URL url) {
		return new URLToolInput<IKT>(inputkey, url);
	}

	public static <IKT> ToolInput<IKT> createForZipEntry(ZipFile zf, ZipEntry entry) {
		return createForZipEntry(null, zf, entry);
	}

	public static <IKT> ToolInput<IKT> createForZipEntry(IKT inputkey, ZipFile zf, ZipEntry entry) {
		return new ZipEntryToolInput<IKT>(inputkey, zf, entry);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((inputKey == null) ? 0 : inputKey.hashCode());
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
		ToolInput<?> other = (ToolInput<?>) obj;
		if (inputKey == null) {
			if (other.inputKey != null)
				return false;
		} else if (!inputKey.equals(other.inputKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (inputKey != null) {
			builder.append("inputKey=");
			builder.append(inputKey);
		}
		builder.append("]");
		return builder.toString();
	}

	private static final class FileToolInput<IKT> extends ToolInput<IKT> {
		private final File classfile;

		private FileToolInput(IKT inputKey, File classfile) {
			super(inputKey);
			this.classfile = classfile;
		}

		@Override
		public byte[] getBytes() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (InputStream fis = new FileInputStream(classfile)) {
				Utils.copyStream(fis, baos);
			}
			return baos.toByteArray();
		}
	}

	private static final class PathToolInput<IKT> extends ToolInput<IKT> {
		private final Path classfile;

		private PathToolInput(IKT inputKey, Path classfile) {
			super(inputKey);
			this.classfile = classfile;
		}

		@Override
		public byte[] getBytes() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (InputStream fis = Files.newInputStream(classfile)) {
				Utils.copyStream(fis, baos);
			}
			return baos.toByteArray();
		}
	}

	private static final class URLToolInput<IKT> extends ToolInput<IKT> {
		private final URL url;

		private URLToolInput(IKT inputKey, URL url) {
			super(inputKey);
			this.url = url;
		}

		@Override
		public byte[] getBytes() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (InputStream fis = url.openStream()) {
				Utils.copyStream(fis, baos);
			}
			return baos.toByteArray();
		}
	}

	private static final class ZipEntryToolInput<IKT> extends ToolInput<IKT> {
		private final ZipFile zf;
		private final ZipEntry entry;

		private ZipEntryToolInput(IKT inputKey, ZipFile zf, ZipEntry entry) {
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
	}

	private static final class BytesToolInput<IKT> extends ToolInput<IKT> {
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
}
