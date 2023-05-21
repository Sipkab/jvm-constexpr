package sipka.jvm.constexpr.tool.log;

public final class MessageLogEntry implements LogEntry {
	private final String message;

	public MessageLogEntry(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}

}
