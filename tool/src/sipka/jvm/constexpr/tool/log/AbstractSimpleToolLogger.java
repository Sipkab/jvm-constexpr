package sipka.jvm.constexpr.tool.log;

public abstract class AbstractSimpleToolLogger implements ToolLogger {
	protected abstract void log(LogEntry entry);
}
