package sipka.jvm.constexpr.tool.options;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class FieldDeconstructorConfiguration extends DeconstructorConfiguration {
	protected final Type descriptor;

	FieldDeconstructorConfiguration(Type fieldOwner, String fieldname, Type descriptor) {
		super(fieldOwner, fieldname);
		this.descriptor = descriptor;
	}

	public Type getFieldDescriptor() {
		return descriptor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
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
		FieldDeconstructorConfiguration other = (FieldDeconstructorConfiguration) obj;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
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
		builder.append(", descriptor=");
		builder.append(descriptor);
		builder.append("]");
		return builder.toString();
	}

}
