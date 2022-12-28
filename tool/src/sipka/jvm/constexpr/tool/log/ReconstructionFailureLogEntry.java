package sipka.jvm.constexpr.tool.log;

import java.util.Collections;
import java.util.List;

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
		// TODO Auto-generated method stub
		return null;
	}

}
