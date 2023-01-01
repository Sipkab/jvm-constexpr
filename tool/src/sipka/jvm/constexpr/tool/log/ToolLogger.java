package sipka.jvm.constexpr.tool.log;

public interface ToolLogger {
	public void log(ReconstructionFailureLogEntry logentry);

	public void log(InstructionReplacementLogEntry logentry);

	public void log(DeconstructorNotConfiguredLogEntry logentry);

	public void log(MultipleInitializationPathLogEntry logentry);

	public void log(DeconstructionFailedLogEntry logentry);

	public void log(ConfigClassMemberNotAvailableLogEntry logentry);

	public void log(IndeterministicToStringLogEntry logentry);
}
