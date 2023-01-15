package sipka.jvm.constexpr.tool;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

/**
 * Contains information about the values and the structure found on the instruction stack.
 * <p>
 * The class contains information about what was found on the method stack based on its instructions. It is usually used
 * to provide a meaningful explanation about how a given value was reconstructed or deconstructed to the stack.
 */
public final class AsmStackInfo {
	private static final AsmStackInfo INSTANCE_NULL = new AsmStackInfo(Kind.CONSTANT, null, null, null, null, null);

	public static final AsmStackInfo[] EMPTY_ASMSTACKINFO_ARRAY = new AsmStackInfo[0];

	/**
	 * The kind of the info.
	 */
	public enum Kind {
		STATIC_METHOD,
		METHOD,
		CONSTRUCTOR,
		STATIC_FIELD,
		FIELD,
		ARRAY,
		ARRAY_LOAD,
		ARRAY_LENGTH,
		CONSTANT,
		OPERATOR,

		;
	}

	private Kind kind;
	private Type type;
	private String name;
	private Type descriptor;
	private Object object;
	private AsmStackInfo[] elements;

	private AsmStackInfo(Kind kind, Type type, String name, Type descriptor, Object object, AsmStackInfo[] elements) {
		this.kind = kind;
		this.type = type;
		this.name = name;
		this.descriptor = descriptor;
		this.object = object;
		this.elements = elements;
	}

	static AsmStackInfo createConstant(Object value) {
		if (value == null) {
			return INSTANCE_NULL;
		}
		return new AsmStackInfo(Kind.CONSTANT, null, null, null, value, null);
	}

	static AsmStackInfo createArray(Type componenttype, AsmStackInfo length, AsmStackInfo[] elements) {
		return new AsmStackInfo(Kind.ARRAY, componenttype, null, null, length, elements);
	}

	static AsmStackInfo createArrayLoad(AsmStackInfo arrayobject, AsmStackInfo index) {
		return new AsmStackInfo(Kind.ARRAY_LOAD, null, null, null, arrayobject, new AsmStackInfo[] { index });
	}

	static AsmStackInfo createArrayLength(AsmStackInfo arrayobject) {
		return new AsmStackInfo(Kind.ARRAY_LENGTH, null, null, null, arrayobject, null);
	}

	static AsmStackInfo createConstructor(Type instancetype, Type methoddescriptor, AsmStackInfo[] arguments) {
		return new AsmStackInfo(Kind.CONSTRUCTOR, instancetype, Utils.CONSTRUCTOR_METHOD_NAME, methoddescriptor, null,
				arguments);
	}

	static AsmStackInfo createStaticMethod(Type methodowner, String methodname, Type methoddescriptor,
			AsmStackInfo[] arguments) {
		return new AsmStackInfo(Kind.STATIC_METHOD, methodowner, methodname, methoddescriptor, null, arguments);
	}

	static AsmStackInfo createMethod(Type methodowner, String methodname, Type methoddescriptor, AsmStackInfo object,
			AsmStackInfo[] arguments) {
		return new AsmStackInfo(Kind.METHOD, methodowner, methodname, methoddescriptor, object, arguments);
	}

	static AsmStackInfo createStaticField(Type fieldowner, String fieldname, Type fielddescriptor) {
		return new AsmStackInfo(Kind.STATIC_FIELD, fieldowner, fieldname, fielddescriptor, null, null);
	}

	static AsmStackInfo createField(Type fieldowner, String fieldname, Type fielddescriptor, AsmStackInfo object) {
		return new AsmStackInfo(Kind.FIELD, fieldowner, fieldname, fielddescriptor, object, null);
	}

	static AsmStackInfo createOperator(int opcode, Type checkcasttype, AsmStackInfo[] operands) {
		return new AsmStackInfo(Kind.OPERATOR, checkcasttype, null, null, opcode, operands);
	}

	/**
	 * Gets the kind.
	 * 
	 * @return The kind.
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Gets the type associated with the info.
	 * <p>
	 * <ul>
	 * <li>{@link Kind#ARRAY}: the component type</li>
	 * <li>{@link Kind#CONSTRUCTOR}: The declaring type of the constructor</li>
	 * <li>{@link Kind#STATIC_METHOD}: The declaring type of the method</li>
	 * <li>{@link Kind#METHOD}: The declaring type of the method</li>
	 * <li>{@link Kind#STATIC_FIELD}: The declaring type of the field</li>
	 * <li>{@link Kind#FIELD}: The declaring type of the field</li>
	 * <li>{@link Kind#OPERATOR}: The cast type if the opcode is {@link Opcodes#CHECKCAST CHECKCAST}</li>
	 * </ul>
	 * 
	 * @return The type or <code>null</code> if not applicable.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Gets the type member name associated with the info.
	 * <p>
	 * <ul>
	 * <li>{@link Kind#CONSTRUCTOR}: The value <code>"&lt;init&gt;"</code></li>
	 * <li>{@link Kind#STATIC_METHOD}: The name of the method</li>
	 * <li>{@link Kind#METHOD}: The name of the method</li>
	 * <li>{@link Kind#STATIC_FIELD}: The name of the field</li>
	 * <li>{@link Kind#FIELD}: The name of the field</li>
	 * </ul>
	 * 
	 * @return The name or <code>null</code> if not applicable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type descriptor associated with the info.
	 * <p>
	 * <ul>
	 * <li>{@link Kind#CONSTRUCTOR}: The method descriptor</li>
	 * <li>{@link Kind#STATIC_METHOD}: The method descriptor</li>
	 * <li>{@link Kind#METHOD}: The method descriptor</li>
	 * <li>{@link Kind#STATIC_FIELD}: The field type descriptor</li>
	 * <li>{@link Kind#FIELD}: The field type descriptor</li>
	 * </ul>
	 * 
	 * @return The type or <code>null</code> if not applicable.
	 */
	public Type getDescriptor() {
		return descriptor;
	}

	/**
	 * Gets the object associated with the info.
	 * <p>
	 * <ul>
	 * <li>{@link Kind#CONSTANT}: The constant object. May be <code>null</code> if the value was <code>null</code>. May
	 * be an instance of {@link Type} instead of {@link Class} if the class wasn't loaded when the object was
	 * reconstructed.</li>
	 * <li>{@link Kind#ARRAY}: The length value stack info. An instance of {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#ARRAY_LOAD}: The array stack info. An instance of {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#ARRAY_LENGTH}: The array stack info. May not contain information about the individual elements.
	 * An instance of {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#METHOD}: The instance object of the method call. The object has a type of
	 * {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#FIELD}: The instance object of the field access. The object has a type of
	 * {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#OPERATOR}: The ASM {@linkplain Opcodes opcode} of the operator. Has an {@link Integer} type.</li>
	 * </ul>
	 * 
	 * @return The object or <code>null</code> if not applicable.
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * Gets the elements associated with the info.
	 * <p>
	 * <ul>
	 * <li>{@link Kind#ARRAY}: The array elements. Has the same length as the array itself, might have <code>null</code>
	 * elements. Might not be filled at all when the array is part of an {@link Kind#ARRAY_LENGTH ARRAY_LENGTH} stack
	 * info.</li>
	 * <li>{@link Kind#ARRAY_LOAD}: The index of the array load instruction. A 1 length array with the 0 index element
	 * as the index stack info, with type of {@link AsmStackInfo}.</li>
	 * <li>{@link Kind#CONSTRUCTOR}: The method arguments.</li>
	 * <li>{@link Kind#STATIC_METHOD}: The method arguments.</li>
	 * <li>{@link Kind#METHOD}: The method arguments.</li>
	 * <li>{@link Kind#OPERATOR}: The operands.</li>
	 * </ul>
	 * 
	 * @return The elements or <code>null</code> if not applicable.
	 */
	public AsmStackInfo[] getElements() {
		return elements;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
		result = prime * result + Arrays.hashCode(elements);
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		AsmStackInfo other = (AsmStackInfo) obj;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		if (!Arrays.equals(elements, other.elements))
			return false;
		if (kind != other.kind)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName());
		sb.append('[');
		switch (kind) {
			case ARRAY: {
				sb.append(kind.name());
				sb.append(" [");
				for (int i = 0; i < elements.length; i++) {
					AsmStackInfo elem = elements[i];
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(elem);
				}
				sb.append("]");
				break;
			}
			case ARRAY_LENGTH: {
				sb.append(kind.name());
				break;
			}
			case CONSTANT:
				sb.append(kind.name());
				sb.append(": ");
				sb.append(object);
				break;
			case CONSTRUCTOR:
				sb.append(kind.name());
				sb.append(" ");
				sb.append(type.getInternalName());
				sb.append(descriptor);
				break;
			case STATIC_FIELD:
			case FIELD:
				sb.append(kind.name());
				sb.append(" ");
				sb.append(descriptor);
				sb.append(" ");
				sb.append(type.getInternalName());
				sb.append(".");
				sb.append(name);
				break;
			case STATIC_METHOD:
			case METHOD:
				sb.append(kind.name());
				sb.append(" ");
				sb.append(type.getInternalName());
				sb.append(".");
				sb.append(name);
				sb.append(descriptor);
				break;
			case OPERATOR:
				sb.append(kind.name());
				sb.append(" ");
				sb.append(Utils.getOpcodeName((int) object));
				sb.append(": ");
				for (int i = 0; i < elements.length; i++) {
					AsmStackInfo elem = elements[i];
					if (i > 0) {
						sb.append(", ");
					}
					sb.append(elem);
				}
				break;
			default: {
				sb.append(kind.name());
				break;
			}
		}
		sb.append(']');
		return sb.toString();
	}

}
