package sipka.jvm.constexpr.tool.options;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public class ExecutableDeconstructorConfiguration extends DeconstructorConfiguration {
	protected final DeconstructionDataAccessor[] executableParameterDataAccessors;

	ExecutableDeconstructorConfiguration(Type memberOwner, String memberName,
			DeconstructionDataAccessor[] executableParameterDataAccessors) {
		super(memberOwner, memberName);
		this.executableParameterDataAccessors = executableParameterDataAccessors;
	}

	public DeconstructionDataAccessor[] getExecutableParameterDataAccessors() {
		return executableParameterDataAccessors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(executableParameterDataAccessors);
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
		ExecutableDeconstructorConfiguration other = (ExecutableDeconstructorConfiguration) obj;
		if (!Arrays.equals(executableParameterDataAccessors, other.executableParameterDataAccessors))
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
		builder.append(", executableParameterTypes=");
		builder.append(Arrays.toString(executableParameterDataAccessors));
		builder.append("]");
		return builder.toString();
	}
}
