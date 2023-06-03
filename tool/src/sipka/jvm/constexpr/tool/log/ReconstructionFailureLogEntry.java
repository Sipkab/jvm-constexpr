package sipka.jvm.constexpr.tool.log;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import sipka.jvm.constexpr.tool.Utils;

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
		String ls = System.lineSeparator();

		StringBuilder sb = new StringBuilder();

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
			Utils.appendThrowableStackTrace(sb, rc);
		}
		return sb.toString();
	}

}
