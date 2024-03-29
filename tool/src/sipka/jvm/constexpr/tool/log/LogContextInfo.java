package sipka.jvm.constexpr.tool.log;

public interface LogContextInfo {
	//may be null
	public BytecodeLocation getBytecodeLocation();

	public String getMessage();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
