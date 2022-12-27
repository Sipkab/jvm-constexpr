package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;

/**
 * Value class that is a unique identifier of a field of a class.
 */
class FieldKey extends MemberKey {

	public FieldKey(String owner, String fieldName) {
		super(owner, fieldName);
	}

	public FieldKey(FieldInsnNode fieldins) {
		this(fieldins.owner, fieldins.name);
	}

	public FieldKey(Field field) {
		this(field.getDeclaringClass(), field.getName());
	}

	public FieldKey(Class<?> type, String fieldname) {
		this(Type.getInternalName(type), fieldname);
	}

	public String getFieldName() {
		return memberName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FieldKey[owner=");
		builder.append(owner);
		builder.append(", memberName=");
		builder.append(memberName);
		builder.append("]");
		return builder.toString();
	}

}