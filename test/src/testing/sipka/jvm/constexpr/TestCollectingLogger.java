package testing.sipka.jvm.constexpr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sipka.jvm.constexpr.tool.log.AbstractSimpleToolLogger;
import sipka.jvm.constexpr.tool.log.LogEntry;

public final class TestCollectingLogger extends AbstractSimpleToolLogger {
	private Set<LogEntry> logEntries = new HashSet<>();
	private Map<Class<? extends LogEntry>, Set<LogEntry>> typedLogEntries = new HashMap<>();

	@Override
	protected void log(LogEntry entry) {
		logEntries.add(entry);
		typedLogEntries.computeIfAbsent(entry.getClass(), x -> new HashSet<>()).add(entry);

		System.err.println(entry.getMessage().trim());
		System.err.println();
	}

	public Set<LogEntry> getLogEntries() {
		return logEntries;
	}

	@SuppressWarnings("unchecked")
	public <T extends LogEntry> Set<? extends T> getLogEntriesForType(Class<T> logtype) {
		return (Set<? extends T>) typedLogEntries.computeIfAbsent(logtype, x -> new HashSet<>());
	}
}