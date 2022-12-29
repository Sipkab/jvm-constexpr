package sipka.jvm.constexpr.tool.log;

public abstract class AbstractSimpleToolLogger implements ToolLogger {
	@Override
	public void log(ReconstructionFailureLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	@Override
	public void log(InstructionReplacementLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	@Override
	public void log(DeconstructorNotConfiguredLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	protected abstract void log(LogEntry entry);
}
