package sipka.jvm.constexpr.tool.options;

public final class MemberReference {
	protected final String ownerInternalName;
	protected final String memberName;
	protected final String memberDescriptor;

	public MemberReference(String ownerInternalName, String memberName, String memberDescriptor) {
		this.ownerInternalName = ownerInternalName;
		this.memberName = memberName;
		this.memberDescriptor = memberDescriptor;
	}

	public String getOwnerInternalName() {
		return ownerInternalName;
	}

	public String getMemberName() {
		return memberName;
	}

	public String getMemberDescriptor() {
		return memberDescriptor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberDescriptor == null) ? 0 : memberDescriptor.hashCode());
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((ownerInternalName == null) ? 0 : ownerInternalName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberReference other = (MemberReference) obj;
		if (memberDescriptor == null) {
			if (other.memberDescriptor != null)
				return false;
		} else if (!memberDescriptor.equals(other.memberDescriptor))
			return false;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (ownerInternalName == null) {
			if (other.ownerInternalName != null)
				return false;
		} else if (!ownerInternalName.equals(other.ownerInternalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[ownerInternalName=");
		builder.append(ownerInternalName);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append(", memberDescriptor=");
		builder.append(memberDescriptor);
		builder.append("]");
		return builder.toString();
	}

}
