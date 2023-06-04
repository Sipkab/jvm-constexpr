package sipka.jvm.constexpr.tool.log;

public class ForwardingToolLogger implements ToolLogger {
	protected final ToolLogger logger;

	public ForwardingToolLogger(ToolLogger logger) {
		this.logger = logger;
	}

	@Override
	public void log(ReconstructionFailureLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(InstructionReplacementLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(DeconstructorNotConfiguredLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(MultipleInitializationPathLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(DeconstructionFailedLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(ConfigClassMemberInaccessibleLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(IndeterministicToStringLogEntry logentry) {
		logger.log(logentry);
	}

	@Override
	public void log(LogEntry entry) {
		logger.log(entry);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[logger=");
		builder.append(logger);
		builder.append("]");
		return builder.toString();
	}

}
