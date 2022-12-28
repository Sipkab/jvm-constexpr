package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.log.LogContextInfo;

class ReconstructionException extends ConstantAnalysisException {
	private static final long serialVersionUID = 1L;

	private LogContextInfo contextInfo;

	public ReconstructionException(Throwable cause, LogContextInfo contextInfo) {
		super(cause);
		this.contextInfo = contextInfo;
	}

	//just a specialized constructor when the exception is forwarded
	public ReconstructionException(ReconstructionException cause, LogContextInfo contextInfo) {
		this((Throwable) cause, contextInfo);
	}

	public LogContextInfo getContextInfo() {
		return contextInfo;
	}

	@Override
	public String getMessage() {
		return contextInfo.getMessage();
	}

	@Override
	public String toString() {
		return getClass().getName() + ": " + getMessage() + System.lineSeparator() + "\t\tin "
				+ contextInfo.getBytecodeLocation();
	}
}
