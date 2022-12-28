package sipka.jvm.constexpr.tool.options;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	public static <IKT> ToolInput<IKT> createForURI(URI uri) {
		return createForURI(null, uri);
	}

	public static <IKT> ToolInput<IKT> createForURI(IKT inputkey, URI uri) {
		return new URIToolInput<IKT>(inputkey, uri);
	}

	public static <IKT> ToolInput<IKT> createForZipEntry(ZipFile zf, ZipEntry entry) {
		return createForZipEntry(null, zf, entry);
	}

	public static <IKT> ToolInput<IKT> createForZipEntry(IKT inputkey, ZipFile zf, ZipEntry entry) {
		return new ZipEntryToolInput<IKT>(inputkey, zf, entry);
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
}
