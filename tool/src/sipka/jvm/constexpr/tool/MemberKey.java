package sipka.jvm.constexpr.tool;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

class MemberKey {
	protected final String owner;
	protected final String memberName;

	public MemberKey(String owner, String memberName) {
		this.owner = owner;
		this.memberName = memberName;
	}

	public static MemberKey create(String owner, String membername, String descriptor) {
		if (descriptor.charAt(0) == '(') {
			//condition based on sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type.getTypeInternal(String, int, int)
			return new MethodKey(owner, membername, descriptor);
		}
		return new FieldKey(owner, membername, descriptor);
	}

	public static MemberKey create(Member member) {
		if (member instanceof Executable) {
			return MethodKey.create((Executable) member);
		}
		return FieldKey.create((Field) member);
	}

	public static String getDescriptor(MemberKey key) {
		if (key instanceof MethodKey) {
			return ((MethodKey) key).getMethodDescriptor();
		}
		if (key instanceof FieldKey) {
			return ((FieldKey) key).getFieldDescriptor();
		}
		if (key == null) {
			return null;
		}
		throw new IllegalArgumentException(
				"Unknown " + MemberKey.class.getSimpleName() + " subclass: " + key.getClass().getName() + ": " + key);
	}

	/**
	 * Ordering is based on a owner-member_name ascending order.
	 * <p>
	 * For a given owner-member_name pair, the order is:
	 * <ol>
	 * <li>Fields</li>
	 * <li>Methods with ascending method descriptor</li>
	 * </ol>
	 */
	public static int compare(MemberKey l, MemberKey r) {
		if (l == null) {
			if (r == null) {
				return 0;
			}
			return -1;
		}
		if (r == null) {
			return 1;
		}

		int cmp = l.owner.compareTo(r.owner);
		if (cmp != 0) {
			return cmp;
		}
		cmp = l.memberName.compareTo(r.memberName);
		if (cmp != 0) {
			return cmp;
		}

		//we know all the implementations so this is an exhaustive comparison
		//order FieldKeys first
		if (l instanceof MethodKey) {
			if (r instanceof MethodKey) {
				return ((MethodKey) l).getMethodDescriptor().compareTo(((MethodKey) r).getMethodDescriptor());
			}
			return 1;
		}
		if (r instanceof MethodKey) {
			return -1;
		}
		//both are field keys

		return ((FieldKey) l).getFieldDescriptor().compareTo(((FieldKey) r).getFieldDescriptor());
	}

	public final String getOwner() {
		return owner;
	}

	public final String getMemberName() {
		return memberName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((memberName == null) ? 0 : memberName.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
		MemberKey other = (MemberKey) obj;
		if (memberName == null) {
			if (other.memberName != null)
				return false;
		} else if (!memberName.equals(other.memberName))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MemberKey[owner=");
		builder.append(owner);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append("]");
		return builder.toString();
	}

}
