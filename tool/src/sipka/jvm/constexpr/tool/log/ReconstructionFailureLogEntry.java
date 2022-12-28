package sipka.jvm.constexpr.tool.log;

import java.util.List;

public class ReconstructionFailureLogEntry implements LogEntry {

	private List<LogContextInfo> contextStack;
	private Throwable rootCause;

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
