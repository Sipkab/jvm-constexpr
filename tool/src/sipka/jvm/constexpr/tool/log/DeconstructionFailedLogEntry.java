package sipka.jvm.constexpr.tool.log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;

public final class DeconstructionFailedLogEntry implements LogEntry {
	private final Object object;

	private final DeconstructionDataAccessor dataAccessor;
	private final Throwable cause;

	public DeconstructionFailedLogEntry(Object object, DeconstructionDataAccessor dataAccessor, Throwable cause) {
		this.object = object;
		this.dataAccessor = dataAccessor;
		this.cause = cause;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();

		String ls = System.lineSeparator();
		sb.append("Failed to deconstruct object: ");
		sb.append(object);
		if (dataAccessor != null) {
			sb.append(ls);
			sb.append("\tBy data accessor: ");
			sb.append(dataAccessor);
		}
		sb.append(ls);

		Throwable rc = getCause();
		if (rc != null) {
			sb.append("Caused by: ");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
				rc.printStackTrace(pw);
			}
			try {
				sb.append(baos.toString("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				//shouldn't happen
				throw new RuntimeException(e);
			}
		}
		return sb.toString();
	}

	/**
	 * Gets the object that was deconstructed.
	 * 
	 * @return The object.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Gets the {@link DeconstructionDataAccessor data accessor} that threw the exception.
	 * 
	 * @return The data accessor, or <code>null</code> if the exception wasn't caused by a data accessor.
	 */
	public DeconstructionDataAccessor getDataAccessor() {
		return dataAccessor;
	}

	/**
	 * Gets the cause of the failure.
	 * 
	 * @return The cause.
	 */
	public Throwable getCause() {
		return cause;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[object=");
		builder.append(object);
		builder.append(", ");
		if (dataAccessor != null) {
			builder.append("dataAccessor=");
			builder.append(dataAccessor);
			builder.append(", ");
		}
		builder.append("cause=");
		builder.append(cause);
		builder.append("]");
		return builder.toString();
	}

}
