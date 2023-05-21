package testing.sipka.jvm.constexpr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sipka.jvm.constexpr.tool.log.InstructionReplacementLogEntry;
import sipka.jvm.constexpr.tool.log.LogEntry;
import sipka.jvm.constexpr.tool.log.ToolLogger;
import testing.saker.SakerTestCase;

public final class TestCollectingLogger implements ToolLogger {
	private Set<LogEntry> logEntries = new HashSet<>();
	private Map<Class<? extends LogEntry>, Set<LogEntry>> typedLogEntries = new HashMap<>();

	@Override
	public void log(InstructionReplacementLogEntry logentry) {
		ToolLogger.super.log(logentry);
		SakerTestCase.assertNotEquals(logentry.getReplacedInfo(), logentry.getReplacementInfo());
	}

	@Override
	public void log(LogEntry entry) {
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