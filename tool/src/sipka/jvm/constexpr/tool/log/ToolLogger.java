package sipka.jvm.constexpr.tool.log;

public interface ToolLogger {
	public void log(ReconstructionFailureLogEntry logentry);

	public void log(InstructionReplacementLogEntry logentry);
}
