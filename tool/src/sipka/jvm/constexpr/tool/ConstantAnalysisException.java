package sipka.jvm.constexpr.tool;

class ConstantAnalysisException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConstantAnalysisException() {
		super();
	}

	protected ConstantAnalysisException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ConstantAnalysisException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstantAnalysisException(String message) {
		super(message);
	}

	public ConstantAnalysisException(Throwable cause) {
		super(cause);
	}
}
