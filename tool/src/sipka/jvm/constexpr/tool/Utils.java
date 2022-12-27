package sipka.jvm.constexpr.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.IntInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

public class Utils {
	private Utils() {
		throw new UnsupportedOperationException();
	}
	
	public static final String CONSTRUCTOR_METHOD_NAME = "<init>";

	public static final String JAVA_LANG_STRING_INTERNAL_NAME = Type.getInternalName(String.class);
	public static final String JAVA_LANG_OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

	private static final Map<String, Class<?>> PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES = new TreeMap<>();
	static {
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Void.class), void.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Boolean.class), boolean.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Character.class), char.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Byte.class), byte.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Short.class), short.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Integer.class), int.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Long.class), long.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Float.class), float.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.put(Type.getInternalName(Double.class), double.class);
	}
	private static final Map<String, Class<?>> PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES = new TreeMap<>();
	static {
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Boolean.class), Boolean.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Character.class), Character.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Byte.class), Byte.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Short.class), Short.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Integer.class), Integer.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Long.class), Long.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Float.class), Float.class);
		PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.put(Type.getInternalName(Double.class), Double.class);
	}
	private static final Set<Class<?>> BOX_CLASSES = new HashSet<>();
	static {
		BOX_CLASSES.addAll(PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_BOX_CLASSES.values());
	}

	private static final Map<Class<?>, Integer> PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE = new HashMap<>();
	static {
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(byte.class, Opcodes.T_BYTE);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(short.class, Opcodes.T_SHORT);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(int.class, Opcodes.T_INT);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(long.class, Opcodes.T_LONG);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(float.class, Opcodes.T_FLOAT);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(double.class, Opcodes.T_DOUBLE);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(boolean.class, Opcodes.T_BOOLEAN);
		PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.put(char.class, Opcodes.T_CHAR);
	}
	private static final Map<Class<?>, Integer> PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE = new HashMap<>();
	static {
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(byte.class, Opcodes.BASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(short.class, Opcodes.SASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(int.class, Opcodes.IASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(long.class, Opcodes.LASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(float.class, Opcodes.FASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(double.class, Opcodes.DASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(boolean.class, Opcodes.BASTORE);
		PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.put(char.class, Opcodes.CASTORE);
	}

	/**
	 * Gets the primitive class (like <code>int.class</code>) for the argument internal name of a boxing type (like
	 * {@link Integer}).
	 * 
	 * @param name
	 *            The internal name of the type.
	 * @return The associated primitive class or <code>null</code> if not applicable.
	 * @see Type#getInternalName()
	 */
	public static Class<?> getPrimitiveClassForBoxedTypeInternalName(String name) {
		return PRIMITIVE_BOX_TYPE_INTERNAL_NAMES_TO_PRIMITIVE_CLASSES.get(name);
	}

	/**
	 * Checks if the argument object is considered to be a constant value.
	 * <p>
	 * It is if <code>null</code>, primitive type, or {@link String}.
	 * 
	 * @param val
	 *            The value.
	 * @return <code>true</code> if constant.
	 */
	public static boolean isConstantValue(Object val) {
		if (val == null || val instanceof String) {
			return true;
		}
		return BOX_CLASSES.contains(val.getClass());
	}

	public static void patchAsmArgTypesWithMethodReturnTypes(Type[] asmargtypes, Method[] methods) {
		for (int i = 0; i < methods.length; i++) {
			if (asmargtypes[i] != null) {
				continue;
			}
			asmargtypes[i] = Type.getType(methods[i].getReturnType());
		}
	}

	public static Method[] getNoArgMethodsWithNames(Class<?> type, String... argumentsgettermethodnames)
			throws NoSuchMethodException {
		Method[] methods = new Method[argumentsgettermethodnames.length];
		for (int i = 0; i < methods.length; i++) {
			methods[i] = type.getMethod(argumentsgettermethodnames[i]);
		}
		return methods;
	}

	public static Type[] toAsmTypes(Class<?>[] parameterTypes) {
		Type[] asmparamtypes = new Type[parameterTypes.length];
		for (int i = 0; i < asmparamtypes.length; i++) {
			asmparamtypes[i] = Type.getType(parameterTypes[i]);
		}
		return asmparamtypes;
	}

	/**
	 * Gets the name of the type as {@link Class#getName()}.
	 * 
	 * @param t
	 *            The type.
	 * @return The name.
	 */
	public static String getNameOfClass(Type t) {
		switch (t.getSort()) {
			case Type.VOID:
				return "void";
			case Type.BOOLEAN:
				return "boolean";
			case Type.CHAR:
				return "char";
			case Type.BYTE:
				return "byte";
			case Type.SHORT:
				return "short";
			case Type.INT:
				return "int";
			case Type.FLOAT:
				return "float";
			case Type.LONG:
				return "long";
			case Type.DOUBLE:
				return "double";
			case Type.ARRAY: {
				StringBuilder sb = new StringBuilder();
				int dimensions = t.getDimensions();
				for (int i = dimensions; i > 0; --i) {
					sb.append('[');
				}
				Type elem = t.getElementType();
				if (elem.getSort() == Type.OBJECT) {
					sb.append('L');
					sb.append(elem.getClassName());
					sb.append(';');
				} else {
					sb.append(elem.getDescriptor());
				}
				return sb.toString();
			}
			case Type.OBJECT:
				return t.getClassName();
			default:
				throw new AssertionError(t);
		}
	}

	/**
	 * Gets the simple name of the type as {@link Class#getSimpleName()}.
	 * 
	 * @param t
	 *            The type.
	 * @return The name.
	 */
	public static String getSimpleNameOfClass(Type t) {
		switch (t.getSort()) {
			case Type.VOID:
				return "void";
			case Type.BOOLEAN:
				return "boolean";
			case Type.CHAR:
				return "char";
			case Type.BYTE:
				return "byte";
			case Type.SHORT:
				return "short";
			case Type.INT:
				return "int";
			case Type.FLOAT:
				return "float";
			case Type.LONG:
				return "long";
			case Type.DOUBLE:
				return "double";
			case Type.ARRAY: {
				Type elem = t.getElementType();
				StringBuilder sb = new StringBuilder(getSimpleNameOfClass(elem));
				int dimensions = t.getDimensions();
				for (int i = dimensions; i > 0; --i) {
					sb.append("[]");
				}
				return sb.toString();
			}
			case Type.OBJECT: {
				String internalname = t.getInternalName();
				int lastslashindex = internalname.lastIndexOf('/');
				String lastname;
				if (lastslashindex >= 0) {
					lastname = internalname.substring(lastslashindex + 1);
				} else {
					lastname = internalname;
				}
				int lastdollarindex = lastname.lastIndexOf('$');
				if (lastdollarindex < 0) {
					return lastname;
				}
				return lastname.substring(lastdollarindex + 1);
			}
			default:
				throw new AssertionError(t);
		}
	}

	/**
	 * Gets the canonical name of the type as {@link Class#getCanonicalName()}.
	 * 
	 * @param t
	 *            The type.
	 * @return The name.
	 */
	public static String getCanonicalNameOfClass(Type t) {
		switch (t.getSort()) {
			case Type.VOID:
				return "void";
			case Type.BOOLEAN:
				return "boolean";
			case Type.CHAR:
				return "char";
			case Type.BYTE:
				return "byte";
			case Type.SHORT:
				return "short";
			case Type.INT:
				return "int";
			case Type.FLOAT:
				return "float";
			case Type.LONG:
				return "long";
			case Type.DOUBLE:
				return "double";
			case Type.ARRAY: {
				Type elem = t.getElementType();
				StringBuilder sb = new StringBuilder(getCanonicalNameOfClass(elem));
				int dimensions = t.getDimensions();
				for (int i = dimensions; i > 0; --i) {
					sb.append("[]");
				}
				return sb.toString();
			}
			case Type.OBJECT: {
				return t.getClassName().replace('$', '.');
			}
			default:
				throw new AssertionError(t);
		}
	}

	/**
	 * Checks if the given method is empty considered its instructions.
	 * <p>
	 * A method is empty, if it returns void, and doesn't have any meaningful instructions in it.
	 * 
	 * @param mn
	 *            The method.
	 * @return <code>true</code> if empty.
	 */
	public static boolean isMethodEmpty(MethodNode mn) {
		for (AbstractInsnNode ins : mn.instructions) {
			switch (ins.getOpcode()) {
				case Opcodes.RETURN:
					//return for a void method, is considered empty
					break;
				default: {
					switch (ins.getType()) {
						case AbstractInsnNode.LINE:
							//irrelevant
							break;
						case AbstractInsnNode.LABEL:
							//irrelevant unless being jumped to or something
							break;
						default: {
							return false;
						}
					}
					break;
				}
			}
		}
		return true;
	}

	/**
	 * Applies an unary operator on the given operand.
	 * <p>
	 * If the operator is numeric, the value will be cast to {@link Number}, and an appropriate
	 * <code>primitiveValue()</code> function is called on it.
	 * <p>
	 * {@link Opcodes#CHECKCAST CHECKCAST} is ignored.
	 * 
	 * @param opcode
	 *            The ASM opcode of the operator.
	 * @param operand
	 *            The operand.
	 * @return The result or <code>null</code> if the operator is unknown.
	 */
	public static Object applyUnaryOperand(int opcode, Object operand) {
		switch (opcode) {
			case Opcodes.CHECKCAST:
				return operand;

			case Opcodes.I2B:
				return ((Number) operand).byteValue();
			case Opcodes.I2S:
				return ((Number) operand).shortValue();
			case Opcodes.L2I:
			case Opcodes.F2I:
			case Opcodes.D2I:
				return ((Number) operand).intValue();
			case Opcodes.I2L:
			case Opcodes.F2L:
			case Opcodes.D2L:
				return ((Number) operand).longValue();
			case Opcodes.I2F:
			case Opcodes.L2F:
			case Opcodes.D2F:
				return ((Number) operand).floatValue();
			case Opcodes.I2D:
			case Opcodes.L2D:
			case Opcodes.F2D:
				return ((Number) operand).doubleValue();
			case Opcodes.I2C:
				return (char) ((Number) operand).intValue();

			case Opcodes.INEG:
				return -((Number) operand).intValue();
			case Opcodes.LNEG:
				return -((Number) operand).longValue();
			case Opcodes.FNEG:
				return -((Number) operand).floatValue();
			case Opcodes.DNEG:
				return -((Number) operand).doubleValue();
			default: {
				return null;
			}
		}
	}

	/**
	 * Applies an binary operator on the given operands.
	 * <p>
	 * If the operator is numeric, the values will be cast to {@link Number}, and an appropriate
	 * <code>primitiveValue()</code> function is called on them.
	 * 
	 * @param opcode
	 *            The ASM opcode of the operator.
	 * @param left
	 *            The left operand.
	 * @param right
	 *            The right operand.
	 * @return The result or <code>null</code> if the operator is unknown.
	 */
	public static Object applyBinaryOperand(int opcode, Object left, Object right) {
		switch (opcode) {
			case Opcodes.IADD:
				return ((Number) left).intValue() + ((Number) right).intValue();
			case Opcodes.ISUB:
				return ((Number) left).intValue() - ((Number) right).intValue();
			case Opcodes.IMUL:
				return ((Number) left).intValue() * ((Number) right).intValue();
			case Opcodes.IDIV:
				return ((Number) left).intValue() / ((Number) right).intValue();
			case Opcodes.IREM:
				return ((Number) left).intValue() % ((Number) right).intValue();
			case Opcodes.ISHL:
				return ((Number) left).intValue() << ((Number) right).intValue();
			case Opcodes.ISHR:
				return ((Number) left).intValue() >> ((Number) right).intValue();
			case Opcodes.IUSHR:
				return ((Number) left).intValue() >>> ((Number) right).intValue();
			case Opcodes.IAND:
				return ((Number) left).intValue() & ((Number) right).intValue();
			case Opcodes.IOR:
				return ((Number) left).intValue() | ((Number) right).intValue();
			case Opcodes.IXOR:
				return ((Number) left).intValue() ^ ((Number) right).intValue();

			case Opcodes.LADD:
				return ((Number) left).longValue() + ((Number) right).longValue();
			case Opcodes.LSUB:
				return ((Number) left).longValue() - ((Number) right).longValue();
			case Opcodes.LMUL:
				return ((Number) left).longValue() * ((Number) right).longValue();
			case Opcodes.LDIV:
				return ((Number) left).longValue() / ((Number) right).longValue();
			case Opcodes.LREM:
				return ((Number) left).longValue() % ((Number) right).longValue();
			case Opcodes.LSHL:
				return ((Number) left).longValue() << ((Number) right).longValue();
			case Opcodes.LSHR:
				return ((Number) left).longValue() >> ((Number) right).longValue();
			case Opcodes.LUSHR:
				return ((Number) left).longValue() >>> ((Number) right).longValue();
			case Opcodes.LAND:
				return ((Number) left).longValue() & ((Number) right).longValue();
			case Opcodes.LOR:
				return ((Number) left).longValue() | ((Number) right).longValue();
			case Opcodes.LXOR:
				return ((Number) left).longValue() ^ ((Number) right).longValue();

			default: {
				return null;
			}
		}
	}

	/**
	 * Creates an array with the given size for the specified operand of a {@link Opcodes#NEWARRAY NEWARRAY}
	 * {@link IntInsnNode} instruction.
	 * 
	 * @param size
	 *            The size.
	 * @param operand
	 *            The operand.
	 * @return The new array.
	 */
	public static Object createAsmArray(int size, int operand) {
		switch (operand) {
			case Opcodes.T_BOOLEAN:
				return new boolean[size];
			case Opcodes.T_CHAR:
				return new char[size];
			case Opcodes.T_BYTE:
				return new byte[size];
			case Opcodes.T_SHORT:
				return new short[size];
			case Opcodes.T_INT:
				return new int[size];
			case Opcodes.T_LONG:
				return new long[size];
			case Opcodes.T_FLOAT:
				return new float[size];
			case Opcodes.T_DOUBLE:
				return new double[size];
			default:
				throw new IllegalArgumentException("Unknown NEWARRAY operand: " + operand);
		}
	}

	public static int getOperandForAsmNewArrayInstruction(Class<?> primitivetype) {
		return PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.get(primitivetype);
	}

	public static int getOperandForAsmStoreArrayInstruction(Class<?> type) {
		Integer opcode = PRIMITIVE_TYPE_TO_ASM_STORE_ARRAY_OPCODE.get(type);
		if (opcode == null) {
			return Opcodes.AASTORE;
		}
		return opcode;
	}

	public static Object asmCastValueToReceiverType(Object val, Class<?> type) {
		if (type == null) {
			//unknown type, can't cast
			return val;
		}
		if (type == byte.class || type == Byte.class) {
			return ((Number) val).byteValue();
		} else if (type == short.class || type == Short.class) {
			return ((Number) val).shortValue();
		} else if (type == int.class || type == Integer.class) {
			return ((Number) val).intValue();
		} else if (type == long.class || type == Long.class) {
			return ((Number) val).longValue();
		} else if (type == float.class || type == Float.class) {
			return ((Number) val).floatValue();
		} else if (type == double.class || type == Double.class) {
			return ((Number) val).doubleValue();
		} else if (type == boolean.class || type == Boolean.class) {
			if (val instanceof Boolean) {
				//ok
				return val;
			}
			return ((Number) val).intValue() != 0;
		} else if (type == char.class || type == Character.class) {
			if (val instanceof Character) {
				//ok
				return val;
			}
			return (char) ((Number) val).intValue();
		} else {
			return val;
		}
	}

	public static boolean isSameTypes(Class<?>[] types, Type[] asmtypes) {
		if (types.length != asmtypes.length) {
			return false;
		}
		for (int i = 0; i < asmtypes.length; i++) {
			if (!Type.getType(types[i]).equals(asmtypes[i])) {
				return false;
			}
		}
		return true;
	}

	public static Method getMethodForInstruction(Class<?> type, MethodInsnNode methodins) throws NoSuchMethodException {
		return getMethodForMethodDescriptor(type, methodins.desc, methodins.name);
	}

	public static Method getMethodForMethodDescriptor(Class<?> type, String descriptor, String name)
			throws NoSuchMethodException {
		Type[] asmparamtypes = Type.getArgumentTypes(descriptor);
		for (Method m : type.getMethods()) {
			if (!isMethodMatchesNameAndDescriptor(m, descriptor, name, asmparamtypes)) {
				continue;
			}
			m.setAccessible(true);
			return m;
		}
		for (Method m : type.getDeclaredMethods()) {
			if (!isMethodMatchesNameAndDescriptor(m, descriptor, name, asmparamtypes)) {
				continue;
			}
			m.setAccessible(true);
			return m;
		}
		throw new NoSuchMethodException(
				"Method not found on " + type + " with name: " + name + " and descriptor: " + descriptor);
	}

	private static boolean isMethodMatchesNameAndDescriptor(Method m, String descriptor, String name,
			Type[] asmparamtypes) {
		if (!name.equals(m.getName())) {
			return false;
		}
		Class<?>[] paramtypes = m.getParameterTypes();
		if (!isSameTypes(paramtypes, asmparamtypes)) {
			return false;
		}
		if (!Type.getType(m.getReturnType()).equals(Type.getReturnType(descriptor))) {
			//just correctness check
			//Java classes don't usually have same methods with different return types, but its possible
			return false;
		}
		return true;
	}

	public static Constructor<?> getConstructorForMethodDescriptor(Class<?> type, String descriptor)
			throws NoSuchMethodException {
		Type[] asmparamtypes = Type.getArgumentTypes(descriptor);
		for (Constructor<?> constructor : type.getConstructors()) {
			Class<?>[] paramtypes = constructor.getParameterTypes();
			if (!isSameTypes(paramtypes, asmparamtypes)) {
				continue;
			}
			constructor.setAccessible(true);
			return constructor;
		}
		throw new NoSuchMethodException("Constructor not found on " + type + " and descriptor: " + descriptor);
	}

	public static boolean isInlineableConstantType(Type fieldtype) {
		switch (fieldtype.getSort()) {
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.FLOAT:
			case Type.LONG:
			case Type.DOUBLE:
				return true;
			case Type.OBJECT:
				if (JAVA_LANG_STRING_INTERNAL_NAME.equals(fieldtype.getInternalName())) {
					return true;
				}
				return false;
			default: {
				return false;
			}
		}
	}

	public static Class<?> getReceiverType(Type fieldtype) {
		switch (fieldtype.getSort()) {
			case Type.BOOLEAN:
				return boolean.class;
			case Type.CHAR:
				return char.class;
			case Type.BYTE:
				return byte.class;
			case Type.SHORT:
				return short.class;
			case Type.INT:
				return int.class;
			case Type.FLOAT:
				return float.class;
			case Type.LONG:
				return long.class;
			case Type.DOUBLE:
				return double.class;
			case Type.OBJECT: {
				String internalname = fieldtype.getInternalName();
				if (JAVA_LANG_STRING_INTERNAL_NAME.equals(internalname)) {
					return String.class;
				}
				if (JAVA_LANG_OBJECT_INTERNAL_NAME.equals(internalname)) {
					return Object.class;
				}
				return null;
			}
			default: {
				return null;
			}
		}
	}

	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024 * 8];
		for (int read; (read = in.read(buf)) > 0;) {
			out.write(buf, 0, read);
		}
	}

	public static void addToInternalNameMap(Map<String, Class<?>> map, Class<?> type) {
		map.put(Type.getInternalName(type), type);
	}

}
