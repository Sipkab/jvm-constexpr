package sipka.jvm.constexpr.tool.log;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public final class ReconstructionFailureLogEntry implements LogEntry {

	private Throwable rootCause;
	private List<LogContextInfo> contextStack;

	public ReconstructionFailureLogEntry(Throwable rootCause, List<LogContextInfo> contextStack) {
		this.rootCause = rootCause;
		this.contextStack = contextStack;
	}

	public List<LogContextInfo> getContextStack() {
		return Collections.unmodifiableList(contextStack);
	}

	public Throwable getRootCause() {
		return rootCause;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();

		String ls = System.lineSeparator();

		List<LogContextInfo> contextstack = getContextStack();
		for (ListIterator<LogContextInfo> it = contextstack.listIterator(contextstack.size()); it.hasPrevious();) {
			LogContextInfo info = it.previous();
			sb.append(info.getMessage());
			sb.append(ls);
			sb.append("\tin " + info.getBytecodeLocation());
			sb.append(ls);
		}
		Throwable rc = getRootCause();
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

}
