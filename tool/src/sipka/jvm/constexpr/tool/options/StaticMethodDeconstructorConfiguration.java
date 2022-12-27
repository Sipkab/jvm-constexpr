package sipka.jvm.constexpr.tool.options;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class StaticMethodDeconstructorConfiguration extends DeconstructorConfiguration {
	protected final Type executableReturnType;
	protected final Type[] executableParameterTypes;
	protected final String[] getterMethodNames;

	StaticMethodDeconstructorConfiguration(Type executableOwner, String executableName,
			Type executableReturnType, Type[] executableParameterTypes, String[] getterMethodNames) {
		super(executableOwner, executableName);
		if (executableParameterTypes.length != getterMethodNames.length) {
			throw new IllegalArgumentException("Mismatch between reconstructor method parameter type count: "
					+ executableParameterTypes.length + " and getter method count: " + getterMethodNames.length);
		}
		this.executableReturnType = executableReturnType;
		this.executableParameterTypes = executableParameterTypes;
		this.getterMethodNames = getterMethodNames;
	}

	public Type getExecutableReturnType() {
		return executableReturnType;
	}

	public Type[] getExecutableParameterTypes() {
		return executableParameterTypes;
	}

	public String[] getGetterMethodNames() {
		return getterMethodNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(executableParameterTypes);
		result = prime * result + ((executableReturnType == null) ? 0 : executableReturnType.hashCode());
		result = prime * result + Arrays.hashCode(getterMethodNames);
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
		if (!Arrays.equals(executableParameterTypes, other.executableParameterTypes))
			return false;
		if (executableReturnType == null) {
			if (other.executableReturnType != null)
				return false;
		} else if (!executableReturnType.equals(other.executableReturnType))
			return false;
		if (!Arrays.equals(getterMethodNames, other.getterMethodNames))
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
		builder.append(Arrays.toString(executableParameterTypes));
		builder.append(", getterMethodNames=");
		builder.append(Arrays.toString(getterMethodNames));
		builder.append("]");
		return builder.toString();
	}

}
