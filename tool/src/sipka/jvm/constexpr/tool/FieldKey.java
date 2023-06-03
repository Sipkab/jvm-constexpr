package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;

/**
 * Value class that is a unique identifier of a field of a class.
 */
class FieldKey extends MemberKey {
	protected final String fieldDescriptor;

	public FieldKey(String owner, String fieldName, String fieldDescriptor) {
		super(owner, fieldName);
		this.fieldDescriptor = fieldDescriptor;
	}

	public FieldKey(FieldInsnNode fieldins) {
		this(fieldins.owner, fieldins.name, fieldins.desc);
	}

	public FieldKey(Field field) {
		this(field.getDeclaringClass(), field.getName(), field.getType());
	}

	public FieldKey(Class<?> type, String fieldname, Class<?> fieldtype) {
		this(Type.getInternalName(type), fieldname, Type.getDescriptor(fieldtype));
	}

	public static FieldKey create(Field field) {
		return new FieldKey(field);
	}

	public String getFieldName() {
		return memberName;
	}

	public String getFieldDescriptor() {
		return fieldDescriptor;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[memberName=");
		builder.append(memberName);
		builder.append(", owner=");
		builder.append(owner);
		builder.append(", fieldDescriptor=");
		builder.append(fieldDescriptor);
		builder.append("]");
		return builder.toString();
	}

}