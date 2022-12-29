package sipka.jvm.constexpr.tool.options;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class StaticMethodDeconstructorConfiguration extends ExecutableDeconstructorConfiguration {
	protected final Type executableReturnType;

	StaticMethodDeconstructorConfiguration(Type executableOwner, String executableName, Type executableReturnType,
			DeconstructionDataAccessor[] executableParameterDataAccessors) {
		super(executableOwner, executableName, executableParameterDataAccessors);
		this.executableReturnType = executableReturnType;
	}

	public Type getExecutableReturnType() {
		return executableReturnType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((executableReturnType == null) ? 0 : executableReturnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticMethodDeconstructorConfiguration other = (StaticMethodDeconstructorConfiguration) obj;
		if (executableReturnType == null) {
			if (other.executableReturnType != null)
				return false;
		} else if (!executableReturnType.equals(other.executableReturnType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[memberOwner=");
		builder.append(memberOwner);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", executableReturnType=");
		builder.append(executableReturnType);
		builder.append(", executableParameterTypes=");
		builder.append(Arrays.toString(executableParameterDataAccessors));
		builder.append("]");
		return builder.toString();
	}

}
