package sipka.jvm.constexpr.tool.options;

import java.util.Objects;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

/**
 * A reference to a Java class member.
 * <p>
 * Might be a method or field, depending on the type of the {@linkplain #getMemberDescriptor() member descriptor}.
 * <p>
 * In some cases a {@link MemberReference} can represent a type, in which case the member name and descriptor will be
 * empty strings.
 */
public final class MemberReference implements Comparable<MemberReference> {
	protected final String ownerInternalName;
	protected final String memberName;
	protected final String memberDescriptor;

	public MemberReference(String ownerInternalName, String memberName, String memberDescriptor) {
		Objects.requireNonNull(ownerInternalName, "ownerInternalName");
		Objects.requireNonNull(memberName, "memberName");
		Objects.requireNonNull(memberDescriptor, "memberDescriptor");

		this.ownerInternalName = ownerInternalName;
		this.memberName = memberName;
		this.memberDescriptor = memberDescriptor;
	}

	/**
	 * Gets the internal name of the member owner.
	 * 
	 * @return The internal name.
	 * @see Type#getInternalName()
	 */
	public String getOwnerInternalName() {
		return ownerInternalName;
	}

	/**
	 * Gets the name of the member.
	 * <p>
	 * The name of the field or method that this member reference represents.
	 * <p>
	 * Maybe an empty string, in which case this {@link MemberReference} represents a type.
	 * 
	 * @return The member name.
	 */
	public String getMemberName() {
		return memberName;
	}

	/**
	 * Gets the descriptor of the member.
	 * <p>
	 * The type or method descriptor of the member.
	 * <p>
	 * Maybe an empty string, in which case this {@link MemberReference} represents a type.
	 * 
	 * @return The descriptor.
	 * @see Type#getMethodDescriptor(java.lang.reflect.Method)
	 * @see Type#getType(Class)
	 */
	public String getMemberDescriptor() {
		return memberDescriptor;
	}

	@Override
	public int compareTo(MemberReference o) {
		int cmp = this.ownerInternalName.compareTo(o.ownerInternalName);
		if (cmp != 0) {
			return cmp;
		}
		cmp = this.memberName.compareTo(o.memberName);
		if (cmp != 0) {
			return cmp;
		}
		return this.memberDescriptor.compareTo(o.memberDescriptor);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ownerInternalName.hashCode();
		result = prime * result + memberName.hashCode();
		result = prime * result + memberDescriptor.hashCode();
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
		return this.compareTo(other) == 0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		if (memberName.isEmpty()) {
			builder.append(Type.getObjectType(ownerInternalName).getClassName());
		} else {
			Utils.appendMemberDescriptorPretty(builder, Type.getType(memberDescriptor),
					Type.getObjectType(ownerInternalName), memberName);
		}
		builder.append("]");
		return builder.toString();
	}

}
