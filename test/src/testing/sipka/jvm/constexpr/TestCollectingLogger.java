package testing.sipka.jvm.constexpr;

import java.util.HashSet;
import java.util.Set;

import sipka.jvm.constexpr.tool.log.AbstractSimpleToolLogger;
import sipka.jvm.constexpr.tool.log.LogEntry;

public final class TestCollectingLogger extends AbstractSimpleToolLogger {
	private Set<LogEntry> logEntries = new HashSet<>();

	@Override
	protected void log(LogEntry entry) {
		logEntries.add(entry);

		System.err.println(entry.getMessage().trim());
		System.err.println();
	}

	public Set<LogEntry> getLogEntries() {
		return logEntries;
	}
}