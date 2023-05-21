package sipka.jvm.constexpr.tool.log;

public interface ToolLogger {
	public default void log(ReconstructionFailureLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(InstructionReplacementLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(DeconstructorNotConfiguredLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(MultipleInitializationPathLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(DeconstructionFailedLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(ConfigClassMemberInaccessibleLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public default void log(IndeterministicToStringLogEntry logentry) {
		this.log((LogEntry) logentry);
	}

	public void log(LogEntry entry);
}
