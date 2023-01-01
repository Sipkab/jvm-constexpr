package sipka.jvm.constexpr.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import sipka.jvm.constexpr.tool.AsmStackInfo.Kind;
import sipka.jvm.constexpr.tool.log.BytecodeLocation;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.IntInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InvokeDynamicInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LabelNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LineNumberNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

public class Utils {

	private Utils() {
		throw new UnsupportedOperationException();
	}

	public static final String CONSTRUCTOR_METHOD_NAME = "<init>";
	public static final String STATIC_INITIALIZER_METHOD_NAME = "<clinit>";

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

	private static final Map<Type, Class<?>> TYPES_TO_CLASSES = new HashMap<>();
	static {
		TYPES_TO_CLASSES.put(Type.getType(void.class), void.class);
		TYPES_TO_CLASSES.put(Type.getType(byte.class), byte.class);
		TYPES_TO_CLASSES.put(Type.getType(short.class), short.class);
		TYPES_TO_CLASSES.put(Type.getType(int.class), int.class);
		TYPES_TO_CLASSES.put(Type.getType(long.class), long.class);
		TYPES_TO_CLASSES.put(Type.getType(float.class), float.class);
		TYPES_TO_CLASSES.put(Type.getType(double.class), double.class);
		TYPES_TO_CLASSES.put(Type.getType(char.class), char.class);
		TYPES_TO_CLASSES.put(Type.getType(boolean.class), boolean.class);

		TYPES_TO_CLASSES.put(Type.getType(Void.class), Void.class);
		TYPES_TO_CLASSES.put(Type.getType(Byte.class), Byte.class);
		TYPES_TO_CLASSES.put(Type.getType(Short.class), Short.class);
		TYPES_TO_CLASSES.put(Type.getType(Integer.class), Integer.class);
		TYPES_TO_CLASSES.put(Type.getType(Long.class), Long.class);
		TYPES_TO_CLASSES.put(Type.getType(Float.class), Float.class);
		TYPES_TO_CLASSES.put(Type.getType(Double.class), Double.class);
		TYPES_TO_CLASSES.put(Type.getType(Character.class), Character.class);
		TYPES_TO_CLASSES.put(Type.getType(Boolean.class), Boolean.class);

		TYPES_TO_CLASSES.put(Type.getType(Object.class), Object.class);
		TYPES_TO_CLASSES.put(Type.getType(String.class), String.class);
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
	private static final Map<Integer, Class<?>> ASM_ARRAY_OPCODE_TO_PRIMITIVE_COMPONENT_TYPE = new TreeMap<>();
	static {
		for (Entry<Class<?>, Integer> entry : PRIMITIVE_TYPE_TO_ASM_ARRAY_OPCODE.entrySet()) {
			ASM_ARRAY_OPCODE_TO_PRIMITIVE_COMPONENT_TYPE.put(entry.getValue(), entry.getKey());
		}
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

	private static final Set<Object> DEFAULT_ZERO_BOXED_VALUES = new HashSet<>();
	static {
		DEFAULT_ZERO_BOXED_VALUES.add(Byte.valueOf((byte) 0));
		DEFAULT_ZERO_BOXED_VALUES.add(Short.valueOf((short) 0));
		DEFAULT_ZERO_BOXED_VALUES.add(Integer.valueOf(0));
		DEFAULT_ZERO_BOXED_VALUES.add(Long.valueOf(0));
		DEFAULT_ZERO_BOXED_VALUES.add(Float.valueOf(0));
		DEFAULT_ZERO_BOXED_VALUES.add(Double.valueOf(0));
		DEFAULT_ZERO_BOXED_VALUES.add(Character.valueOf('\0'));
		DEFAULT_ZERO_BOXED_VALUES.add(Boolean.valueOf(false));
	}

	private static final String[] OPCODE_NAMES;
	static {
		String[] arr = new String[200];
		arr[Opcodes.NOP] = "NOP";
		arr[Opcodes.ACONST_NULL] = "ACONST_NULL";
		arr[Opcodes.ICONST_M1] = "ICONST_M1";
		arr[Opcodes.ICONST_0] = "ICONST_0";
		arr[Opcodes.ICONST_1] = "ICONST_1";
		arr[Opcodes.ICONST_2] = "ICONST_2";
		arr[Opcodes.ICONST_3] = "ICONST_3";
		arr[Opcodes.ICONST_4] = "ICONST_4";
		arr[Opcodes.ICONST_5] = "ICONST_5";
		arr[Opcodes.LCONST_0] = "LCONST_0";
		arr[Opcodes.LCONST_1] = "LCONST_1";
		arr[Opcodes.FCONST_0] = "FCONST_0";
		arr[Opcodes.FCONST_1] = "FCONST_1";
		arr[Opcodes.FCONST_2] = "FCONST_2";
		arr[Opcodes.DCONST_0] = "DCONST_0";
		arr[Opcodes.DCONST_1] = "DCONST_1";
		arr[Opcodes.BIPUSH] = "BIPUSH";
		arr[Opcodes.SIPUSH] = "SIPUSH";
		arr[Opcodes.LDC] = "LDC";
		arr[Opcodes.ILOAD] = "ILOAD";
		arr[Opcodes.LLOAD] = "LLOAD";
		arr[Opcodes.FLOAD] = "FLOAD";
		arr[Opcodes.DLOAD] = "DLOAD";
		arr[Opcodes.ALOAD] = "ALOAD";
		arr[Opcodes.IALOAD] = "IALOAD";
		arr[Opcodes.LALOAD] = "LALOAD";
		arr[Opcodes.FALOAD] = "FALOAD";
		arr[Opcodes.DALOAD] = "DALOAD";
		arr[Opcodes.AALOAD] = "AALOAD";
		arr[Opcodes.BALOAD] = "BALOAD";
		arr[Opcodes.CALOAD] = "CALOAD";
		arr[Opcodes.SALOAD] = "SALOAD";
		arr[Opcodes.ISTORE] = "ISTORE";
		arr[Opcodes.LSTORE] = "LSTORE";
		arr[Opcodes.FSTORE] = "FSTORE";
		arr[Opcodes.DSTORE] = "DSTORE";
		arr[Opcodes.ASTORE] = "ASTORE";
		arr[Opcodes.IASTORE] = "IASTORE";
		arr[Opcodes.LASTORE] = "LASTORE";
		arr[Opcodes.FASTORE] = "FASTORE";
		arr[Opcodes.DASTORE] = "DASTORE";
		arr[Opcodes.AASTORE] = "AASTORE";
		arr[Opcodes.BASTORE] = "BASTORE";
		arr[Opcodes.CASTORE] = "CASTORE";
		arr[Opcodes.SASTORE] = "SASTORE";
		arr[Opcodes.POP] = "POP";
		arr[Opcodes.POP2] = "POP2";
		arr[Opcodes.DUP] = "DUP";
		arr[Opcodes.DUP_X1] = "DUP_X1";
		arr[Opcodes.DUP_X2] = "DUP_X2";
		arr[Opcodes.DUP2] = "DUP2";
		arr[Opcodes.DUP2_X1] = "DUP2_X1";
		arr[Opcodes.DUP2_X2] = "DUP2_X2";
		arr[Opcodes.SWAP] = "SWAP";
		arr[Opcodes.IADD] = "IADD";
		arr[Opcodes.LADD] = "LADD";
		arr[Opcodes.FADD] = "FADD";
		arr[Opcodes.DADD] = "DADD";
		arr[Opcodes.ISUB] = "ISUB";
		arr[Opcodes.LSUB] = "LSUB";
		arr[Opcodes.FSUB] = "FSUB";
		arr[Opcodes.DSUB] = "DSUB";
		arr[Opcodes.IMUL] = "IMUL";
		arr[Opcodes.LMUL] = "LMUL";
		arr[Opcodes.FMUL] = "FMUL";
		arr[Opcodes.DMUL] = "DMUL";
		arr[Opcodes.IDIV] = "IDIV";
		arr[Opcodes.LDIV] = "LDIV";
		arr[Opcodes.FDIV] = "FDIV";
		arr[Opcodes.DDIV] = "DDIV";
		arr[Opcodes.IREM] = "IREM";
		arr[Opcodes.LREM] = "LREM";
		arr[Opcodes.FREM] = "FREM";
		arr[Opcodes.DREM] = "DREM";
		arr[Opcodes.INEG] = "INEG";
		arr[Opcodes.LNEG] = "LNEG";
		arr[Opcodes.FNEG] = "FNEG";
		arr[Opcodes.DNEG] = "DNEG";
		arr[Opcodes.ISHL] = "ISHL";
		arr[Opcodes.LSHL] = "LSHL";
		arr[Opcodes.ISHR] = "ISHR";
		arr[Opcodes.LSHR] = "LSHR";
		arr[Opcodes.IUSHR] = "IUSHR";
		arr[Opcodes.LUSHR] = "LUSHR";
		arr[Opcodes.IAND] = "IAND";
		arr[Opcodes.LAND] = "LAND";
		arr[Opcodes.IOR] = "IOR";
		arr[Opcodes.LOR] = "LOR";
		arr[Opcodes.IXOR] = "IXOR";
		arr[Opcodes.LXOR] = "LXOR";
		arr[Opcodes.IINC] = "IINC";
		arr[Opcodes.I2L] = "I2L";
		arr[Opcodes.I2F] = "I2F";
		arr[Opcodes.I2D] = "I2D";
		arr[Opcodes.L2I] = "L2I";
		arr[Opcodes.L2F] = "L2F";
		arr[Opcodes.L2D] = "L2D";
		arr[Opcodes.F2I] = "F2I";
		arr[Opcodes.F2L] = "F2L";
		arr[Opcodes.F2D] = "F2D";
		arr[Opcodes.D2I] = "D2I";
		arr[Opcodes.D2L] = "D2L";
		arr[Opcodes.D2F] = "D2F";
		arr[Opcodes.I2B] = "I2B";
		arr[Opcodes.I2C] = "I2C";
		arr[Opcodes.I2S] = "I2S";
		arr[Opcodes.LCMP] = "LCMP";
		arr[Opcodes.FCMPL] = "FCMPL";
		arr[Opcodes.FCMPG] = "FCMPG";
		arr[Opcodes.DCMPL] = "DCMPL";
		arr[Opcodes.DCMPG] = "DCMPG";
		arr[Opcodes.IFEQ] = "IFEQ";
		arr[Opcodes.IFNE] = "IFNE";
		arr[Opcodes.IFLT] = "IFLT";
		arr[Opcodes.IFGE] = "IFGE";
		arr[Opcodes.IFGT] = "IFGT";
		arr[Opcodes.IFLE] = "IFLE";
		arr[Opcodes.IF_ICMPEQ] = "IF_ICMPEQ";
		arr[Opcodes.IF_ICMPNE] = "IF_ICMPNE";
		arr[Opcodes.IF_ICMPLT] = "IF_ICMPLT";
		arr[Opcodes.IF_ICMPGE] = "IF_ICMPGE";
		arr[Opcodes.IF_ICMPGT] = "IF_ICMPGT";
		arr[Opcodes.IF_ICMPLE] = "IF_ICMPLE";
		arr[Opcodes.IF_ACMPEQ] = "IF_ACMPEQ";
		arr[Opcodes.IF_ACMPNE] = "IF_ACMPNE";
		arr[Opcodes.GOTO] = "GOTO";
		arr[Opcodes.JSR] = "JSR";
		arr[Opcodes.RET] = "RET";
		arr[Opcodes.TABLESWITCH] = "TABLESWITCH";
		arr[Opcodes.LOOKUPSWITCH] = "LOOKUPSWITCH";
		arr[Opcodes.IRETURN] = "IRETURN";
		arr[Opcodes.LRETURN] = "LRETURN";
		arr[Opcodes.FRETURN] = "FRETURN";
		arr[Opcodes.DRETURN] = "DRETURN";
		arr[Opcodes.ARETURN] = "ARETURN";
		arr[Opcodes.RETURN] = "RETURN";
		arr[Opcodes.GETSTATIC] = "GETSTATIC";
		arr[Opcodes.PUTSTATIC] = "PUTSTATIC";
		arr[Opcodes.GETFIELD] = "GETFIELD";
		arr[Opcodes.PUTFIELD] = "PUTFIELD";
		arr[Opcodes.INVOKEVIRTUAL] = "INVOKEVIRTUAL";
		arr[Opcodes.INVOKESPECIAL] = "INVOKESPECIAL";
		arr[Opcodes.INVOKESTATIC] = "INVOKESTATIC";
		arr[Opcodes.INVOKEINTERFACE] = "INVOKEINTERFACE";
		arr[Opcodes.INVOKEDYNAMIC] = "INVOKEDYNAMIC";
		arr[Opcodes.NEW] = "NEW";
		arr[Opcodes.NEWARRAY] = "NEWARRAY";
		arr[Opcodes.ANEWARRAY] = "ANEWARRAY";
		arr[Opcodes.ARRAYLENGTH] = "ARRAYLENGTH";
		arr[Opcodes.ATHROW] = "ATHROW";
		arr[Opcodes.CHECKCAST] = "CHECKCAST";
		arr[Opcodes.INSTANCEOF] = "INSTANCEOF";
		arr[Opcodes.MONITORENTER] = "MONITORENTER";
		arr[Opcodes.MONITOREXIT] = "MONITOREXIT";
		arr[Opcodes.MULTIANEWARRAY] = "MULTIANEWARRAY";
		arr[Opcodes.IFNULL] = "IFNULL";
		arr[Opcodes.IFNONNULL] = "IFNONNULL";
		OPCODE_NAMES = arr;
	}

	public static String getOpcodeName(int opcode) {
		try {
			return OPCODE_NAMES[opcode];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e;
		}
	}

	public static Class<?> getClassForType(Type type) {
		if (type == null) {
			return null;
		}
		if (type.getSort() == Type.ARRAY) {
			Class<?> elemclass = getClassForType(type.getElementType());
			if (elemclass == null) {
				return null;
			}
			return Array.newInstance(elemclass, 0).getClass();
		}
		return TYPES_TO_CLASSES.get(type);
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

	public static Class<?> getComponentTypeForAsmNewArrayOperandInstruction(int operand) {
		return ASM_ARRAY_OPCODE_TO_PRIMITIVE_COMPONENT_TYPE.get(operand);
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

	public static Field getFieldForDescriptor(Class<?> type, String name, String descriptor)
			throws NoSuchFieldException {
		for (Field f : type.getDeclaredFields()) {
			if (!f.getName().equals(name)) {
				continue;
			}
			if (descriptor.equals(Type.getDescriptor(f.getType()))) {
				f.setAccessible(true);
				return f;
			}
		}
		throw new NoSuchFieldException(
				"Field not found on " + type + " with name: " + name + " and descriptor: " + descriptor);
	}

	public static Method getMethodForInstruction(Class<?> type, MethodInsnNode methodins) throws NoSuchMethodException {
		return getMethodForMethodDescriptor(type, methodins.owner, methodins.desc, methodins.name);
	}

	public static Method getMethodForMethodDescriptor(Class<?> type, String owner, String descriptor, String name)
			throws NoSuchMethodException {
		Type[] asmparamtypes = Type.getArgumentTypes(descriptor);
		Method m = searchMethodForMethodDescriptor(type, asmparamtypes, owner, descriptor, name, false);
		if (m != null) {
			m.setAccessible(true);
			return m;
		}
		throw new NoSuchMethodException(
				"Method not found on " + type + " with name: " + name + " and descriptor: " + descriptor);
	}

	private static Method searchMethodForMethodDescriptor(Class<?> type, Type[] asmparamtypes, String owner,
			String descriptor, String name, boolean hadowner) {
		if (!hadowner) {
			if (owner.equals(Type.getInternalName(type))) {
				hadowner = true;
			}
		}
		if (hadowner) {
			for (Method m : type.getDeclaredMethods()) {
				if (!isMethodMatchesNameAndDescriptor(m, descriptor, name, asmparamtypes)) {
					continue;
				}
				m.setAccessible(true);
				return m;
			}
		}
		Class<?> sc = type.getSuperclass();
		if (sc != null) {
			Method m = searchMethodForMethodDescriptor(sc, asmparamtypes, owner, descriptor, name, hadowner);
			if (m != null) {
				return m;
			}
		}
		for (Class<?> itf : type.getInterfaces()) {
			Method m = searchMethodForMethodDescriptor(itf, asmparamtypes, owner, descriptor, name, hadowner);
			if (m != null) {
				return m;
			}
		}
		return null;
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

	public static Executable getExecutableForDescriptor(Class<?> type, String owner, String name, String descriptor)
			throws NoSuchMethodException {
		if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
			String typeinternalname = Type.getInternalName(type);
			if (!typeinternalname.equals(owner)) {
				throw new NoSuchMethodException(
						"Constructor not found in type: " + typeinternalname + " with owner: " + owner);
			}
			return getConstructorForMethodDescriptor(type, descriptor);
		}
		return getMethodForMethodDescriptor(type, owner, descriptor, name);
	}

	public static Constructor<?> getConstructorForMethodDescriptor(Class<?> type, String descriptor)
			throws NoSuchMethodException {
		Type[] asmparamtypes = Type.getArgumentTypes(descriptor);
		for (Constructor<?> constructor : type.getDeclaredConstructors()) {
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

	public static boolean isConstantInlineableAsLdc(Object val, Type fieldtype) {
		switch (fieldtype.getSort()) {
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			case Type.FLOAT:
			case Type.LONG:
			case Type.DOUBLE:
				if (val == null) {
					//this shouldn't really happen, but just in case
					return false;
				}
				return true;
			case Type.OBJECT:
				//only String can be inlined if the type of the field is non-primitive
				if (!(val instanceof String)) {
					return false;
				}
				return true;
			default: {
				return false;
			}
		}
	}

	public static Object getDefaultValue(Type type) {
		switch (type.getSort()) {
			case Type.BOOLEAN:
				return false;
			case Type.CHAR:
				return '\0';
			case Type.BYTE:
				return (byte) 0;
			case Type.SHORT:
				return (short) 0;
			case Type.INT:
				return 0;
			case Type.FLOAT:
				return (float) 0;
			case Type.LONG:
				return (long) 0;
			case Type.DOUBLE:
				return (double) 0;
			default: {
				return null;
			}
		}
	}

	public static boolean isZeroDefaultValue(Object o) {
		return o == null || DEFAULT_ZERO_BOXED_VALUES.contains(o);
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

	public static byte[] readStream(InputStream in) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			copyStream(in, baos);
			return baos.toByteArray();
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

	public static boolean isUnaryOperator(int opcode) {
		switch (opcode) {
			case Opcodes.CHECKCAST:
			case Opcodes.I2L:
			case Opcodes.I2F:
			case Opcodes.I2D:
			case Opcodes.I2B:
			case Opcodes.I2C:
			case Opcodes.I2S:
			case Opcodes.INEG:
			case Opcodes.L2I:
			case Opcodes.L2F:
			case Opcodes.L2D:
			case Opcodes.LNEG:
			case Opcodes.F2I:
			case Opcodes.F2L:
			case Opcodes.F2D:
			case Opcodes.FNEG:
			case Opcodes.D2I:
			case Opcodes.D2L:
			case Opcodes.D2F:
			case Opcodes.DNEG:
				return true;
			default:
				return false;
		}
	}

	private static LineNumberNode findLineNumberNode(MethodNode method, LabelNode labelnode) {
		for (AbstractInsnNode ins = method.instructions.getFirst(); ins != null; ins = ins.getNext()) {
			if (ins.getType() != AbstractInsnNode.LINE) {
				continue;
			}
			LineNumberNode ln = (LineNumberNode) ins;
			if (ln.start.equals(labelnode)) {
				return ln;
			}
		}
		return null;
	}

	public static int getLineNumber(MethodNode method, AbstractInsnNode ins) {
		for (AbstractInsnNode it = ins.getPrevious(); it != null; it = it.getPrevious()) {
			switch (it.getType()) {
				case AbstractInsnNode.LABEL: {
					LineNumberNode ln = findLineNumberNode(method, (LabelNode) it);
					if (ln != null) {
						return ln.line;
					}
					break;
				}
				default: {
					break;
				}
			}
		}
		return -1;
	}

	public static boolean isSameInsruction(AbstractInsnNode l, AbstractInsnNode r) {
		if (l == null) {
			return r == null;
		}
		if (r == null) {
			return false;
		}
		if (l.getOpcode() != r.getOpcode()) {
			return false;
		}
		if (l instanceof MethodInsnNode) {
			return isSameInsruction((MethodInsnNode) l, r);
		}
		if (l instanceof FieldInsnNode) {
			return isSameInsruction((FieldInsnNode) l, r);
		}
		if (l instanceof InsnNode) {
			return true;
		}
		if (l instanceof IntInsnNode) {
			IntInsnNode ln = (IntInsnNode) l;
			IntInsnNode rn = (IntInsnNode) r;
			return ln.operand == rn.operand;
		}
		if (l instanceof LdcInsnNode) {
			LdcInsnNode ln = (LdcInsnNode) l;
			LdcInsnNode rn = (LdcInsnNode) r;
			return ln.cst.equals(rn.cst);
		}
		if (l instanceof TypeInsnNode) {
			TypeInsnNode ln = (TypeInsnNode) l;
			TypeInsnNode rn = (TypeInsnNode) r;
			return ln.desc.equals(rn.desc);
		}
		throw new UnsupportedOperationException(l.getClass().getName() + ": " + Integer.toString(l.getOpcode()));
	}

	public static boolean isSameInsruction(FieldInsnNode l, AbstractInsnNode r) {
		if (l == null) {
			return r == null;
		}
		if (r == null) {
			return false;
		}
		if (l.getOpcode() != r.getOpcode()) {
			return false;
		}
		FieldInsnNode rn = (FieldInsnNode) r;
		if (!l.owner.equals(rn.owner)) {
			return false;
		}
		if (!l.name.equals(rn.name)) {
			return false;
		}
		if (!l.desc.equals(rn.desc)) {
			return false;
		}
		return true;
	}

	public static boolean isSameInsruction(MethodInsnNode l, AbstractInsnNode r) {
		if (l == null) {
			return r == null;
		}
		if (r == null) {
			return false;
		}
		if (l.getOpcode() != r.getOpcode()) {
			return false;
		}
		MethodInsnNode rn = (MethodInsnNode) r;
		if (!l.owner.equals(rn.owner)) {
			return false;
		}
		if (!l.name.equals(rn.name)) {
			return false;
		}
		if (!l.desc.equals(rn.desc)) {
			return false;
		}
		if (l.itf != rn.itf) {
			return false;
		}
		return true;
	}

	public static BytecodeLocation getBytecodeLocation(TransformedClass transclass, MethodNode methodnode,
			AbstractInsnNode locationins) {
		int line = getLineNumber(methodnode, locationins);
		return new BytecodeLocation(transclass.input, transclass.classNode.name, methodnode.name, methodnode.desc,
				line);
	}

	public static String memberDescriptorToPrettyString(Type desctype, Type classtype, String name) {
		StringBuilder sb = new StringBuilder();
		appendMemberDescriptorPretty(sb, desctype, classtype, name);
		return sb.toString();
	}

	public static void appendMemberDescriptorPretty(StringBuilder sb, Type desctype, Type classtype, String name) {
		if (desctype.getSort() == Type.METHOD) {
			//method descriptor
			sb.append(classtype.getClassName());
			if (CONSTRUCTOR_METHOD_NAME.equals(name)) {
				//constructor, no need for the name of the method
			} else {
				sb.append(".");
				sb.append(name);
			}
			Type[] argtypes = desctype.getArgumentTypes();
			sb.append('(');
			for (int i = 0; i < argtypes.length; i++) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(argtypes[i].getClassName());
			}
			sb.append(')');
		} else {
			//field descriptor
			sb.append(desctype.getClassName());
			sb.append(' ');
			sb.append(classtype.getClassName());
			sb.append('.');
			sb.append(name);
		}
	}

	public static String formatConstant(Object value) {
		//based on com.sun.tools.javac.util.Constants.format(Object)
		if (value == null) {
			return "null";
		}
		if (value instanceof Byte) {
			return String.format("(byte)0x%02x", (byte) value);
		}
		if (value instanceof Short) {
			return String.format("(short)%d", (short) value);
		}
		if (value instanceof Long) {
			return ((long) value) + "L";
		}
		if (value instanceof Float) {
			float f = (float) value;

			if (Float.isNaN(f) || Float.isInfinite(f)) {
				return value.toString();
			}
			return f + "f";
		}
		if (value instanceof Double) {
			double d = (double) value;

			if (Double.isNaN(d) || Double.isInfinite(d)) {
				return value.toString();
			}
			return d + "";
		}
		if (value instanceof Character) {
			char c = (char) value;
			return '\'' + quote(c) + '\'';
		}
		if (value instanceof String) {
			String s = (String) value;
			return '\"' + quote(s) + '\"';
		}
		if (value instanceof Integer || value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof Type) {
			return ((Type) value).getClassName() + ".class";
		}
		if (value instanceof Class<?>) {
			return ((Class<?>) value).getCanonicalName() + ".class";
		}
		//brackets to signal the unknown typeness of it
		return '<' + value.getClass().getName() + ": " + value.toString() + '>';
	}

	private static String quote(String s) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			buf.append(quote(s.charAt(i)));
		}
		return buf.toString();
	}

	//based on com.sun.tools.javac.util.Convert.quote(char)
	private static String quote(char ch) {
		switch (ch) {
			case '\b':
				return "\\b";
			case '\f':
				return "\\f";
			case '\n':
				return "\\n";
			case '\r':
				return "\\r";
			case '\t':
				return "\\t";
			case '\'':
				return "\\'";
			case '\"':
				return "\\\"";
			case '\\':
				return "\\\\";
			default:
				return (isPrintableAscii(ch)) ? String.valueOf(ch) : String.format("\\u%04x", (int) ch);
		}
	}

	//based on com.sun.tools.javac.util.Convert.isPrintableAscii(char)
	private static boolean isPrintableAscii(char ch) {
		return ch >= ' ' && ch <= '~';
	}

	public static void appendAsmStackInfo(StringBuilder sb, AsmStackInfo info, String indentation) {
		sb.append(indentation);
		switch (info.getKind()) {
			case CONSTANT:
				sb.append(formatConstant(info.getObject()));
				return;
			case METHOD: {
				AsmStackInfo obj = (AsmStackInfo) info.getObject();
				AsmStackInfo[] args = info.getElements();

				appendAsmStackInfo(sb, obj, "");
				sb.append('.');
				sb.append(info.getName());
				appendAsmStackInfoArgs(sb, args);
				return;
			}
			case STATIC_METHOD: {
				AsmStackInfo[] args = info.getElements();

				sb.append(info.getType().getClassName());
				sb.append('.');
				sb.append(info.getName());
				appendAsmStackInfoArgs(sb, args);
				return;
			}
			case CONSTRUCTOR: {
				AsmStackInfo[] args = info.getElements();

				sb.append("new ");
				sb.append(info.getType().getClassName());
				appendAsmStackInfoArgs(sb, args);
				return;
			}
			case STATIC_FIELD: {
				sb.append(info.getType().getClassName());
				sb.append('.');
				sb.append(info.getName());
				return;
			}
			case FIELD: {
				AsmStackInfo obj = (AsmStackInfo) info.getObject();

				appendAsmStackInfo(sb, obj, "");
				sb.append('.');
				sb.append(info.getName());
				return;
			}
			case OPERATOR: {
				int opcode = (int) info.getObject();
				String opstr = getOperatorString(opcode);
				AsmStackInfo[] elems = info.getElements();
				if (opcode == Opcodes.CHECKCAST) {
					sb.append("((");
					sb.append(info.getType().getClassName());
					sb.append(") ");
					appendAsmStackInfo(sb, elems[0], "");
					sb.append(')');
				} else if (elems.length == 1) {
					//unary operator
					sb.append(opstr);
					appendAsmStackInfo(sb, elems[0], "");
				} else {
					appendAsmStackInfo(sb, elems[0], "");
					sb.append(' ');
					sb.append(opstr);
					sb.append(' ');
					appendAsmStackInfo(sb, elems[1], "");
				}

				return;
			}
			case ARRAY: {
				Type componenttype = info.getType();
				AsmStackInfo[] elements = info.getElements();
				sb.append("new ");
				sb.append(componenttype.getClassName());
				sb.append('[');
				sb.append(elements.length);
				sb.append(']');
				if (!isAllNullElements(elements)) {
					Object defaultval = getDefaultValue(info.getType());

					sb.append(" { ");
					int lastdefault = -1;
					for (int i = 0; i < elements.length; i++) {
						AsmStackInfo elem = elements[i];
						boolean isdefault = elem == null;
						if (isdefault) {
							if (lastdefault < 0) {
								lastdefault = i;
							}
							//else multiple default elements after each other
							//nothing to print now
							continue;
						}
						if (lastdefault >= 0) {
							//print the default elements if there were any 
							asmStackInfoAppendArrayDefaultElementValues(sb, defaultval, lastdefault, i);
							lastdefault = -1;
						}

						if (i != 0) {
							sb.append(", ");
						}
						appendAsmArrayElement(sb, componenttype, elem);
					}
					if (lastdefault >= 0) {
						//print the default elements if there were any 
						asmStackInfoAppendArrayDefaultElementValues(sb, defaultval, lastdefault, elements.length);
						lastdefault = -1;
					}
					sb.append(" }");
				}
				return;
			}
			case ARRAY_LOAD: {
				AsmStackInfo arrinfo = (AsmStackInfo) info.getObject();
				AsmStackInfo indexinfo = info.getElements()[0];
				if (arrinfo.getKind() == Kind.ARRAY && indexinfo.getKind() == Kind.CONSTANT) {
					//only display the loaded item, to reduce verbosity
					Type componenttype = arrinfo.getType();
					AsmStackInfo[] elements = arrinfo.getElements();
					int index = ((Number) indexinfo.getObject()).intValue();
					boolean allnullelems = isAllNullElements(elements);
					if (allnullelems) {
						sb.append('(');
					}
					sb.append("new ");
					sb.append(componenttype.getClassName());
					sb.append('[');
					sb.append(elements.length);
					sb.append(']');
					if (allnullelems) {
						//no need for the init block
						//but put the new clause in parentheses, so it doesn't seem like a multi dimensional array
						sb.append(')');
					} else {
						sb.append(" { ");
						if (index != 0) {
							sb.append("..., ");
						}
						appendAsmArrayElement(sb, componenttype, elements[index]);
						if (index + 1 < elements.length) {
							sb.append(", ...");
						}
						sb.append(" }");
					}
				} else {
					appendAsmStackInfo(sb, arrinfo, "");
				}
				sb.append('[');
				appendAsmStackInfo(sb, indexinfo, "");
				sb.append(']');
				return;
			}
			case ARRAY_LENGTH: {
				AsmStackInfo arrinfo = (AsmStackInfo) info.getObject();

				if (arrinfo.getKind() == Kind.ARRAY) {
					//don't display the elements
					Type componenttype = arrinfo.getType();
					AsmStackInfo[] elements = arrinfo.getElements();
					sb.append("new ");
					sb.append(componenttype.getClassName());
					sb.append('[');
					sb.append(elements.length);
					sb.append(']');
					if (!isAllNullElements(elements)) {
						sb.append(" { ... }");
					}
				} else {
					appendAsmStackInfo(sb, arrinfo, "");
				}
				sb.append(".length");
				break;
			}
			default: {
				sb.append(info);
				break;
			}
		}
	}

	private static void asmStackInfoAppendArrayDefaultElementValues(StringBuilder sb, Object defaultval,
			int lastdefault, int i) {
		if (lastdefault > 0) {
			sb.append(", ");
		}
		if (lastdefault == i - 1) {
			//only a single item
			sb.append("[def:");
			sb.append(formatConstant(defaultval));
			sb.append(']');
		} else {
			sb.append('[');
			sb.append(lastdefault);
			sb.append('-');
			sb.append(i - 1);
			sb.append(" def:");
			sb.append(formatConstant(defaultval));
			sb.append(']');
		}
	}

	private static boolean isAllNullElements(Object[] array) {
		for (Object o : array) {
			if (o != null) {
				return false;
			}
		}
		return true;
	}

	private static void appendAsmArrayElement(StringBuilder sb, Type componenttype, AsmStackInfo elem) {
		if (elem == null) {
			sb.append(formatConstant(getDefaultValue(componenttype)));
		} else {
			appendAsmStackInfo(sb, elem, "");
		}
	}

	private static String getOperatorString(int opcode) {
		switch (opcode) {
			case Opcodes.IADD:
				return "+";
			case Opcodes.ISUB:
				return "-";
			case Opcodes.IMUL:
				return "*";
			case Opcodes.IDIV:
				return "/";
			case Opcodes.IREM:
				return "%";
			case Opcodes.ISHL:
				return "<<";
			case Opcodes.ISHR:
				return ">>";
			case Opcodes.IUSHR:
				return ">>>";
			case Opcodes.IAND:
				return "&";
			case Opcodes.IOR:
				return "|";
			case Opcodes.IXOR:
				return "^";

			case Opcodes.LADD:
				return "+";
			case Opcodes.LSUB:
				return "-";
			case Opcodes.LMUL:
				return "*";
			case Opcodes.LDIV:
				return "/";
			case Opcodes.LREM:
				return "%";
			case Opcodes.LSHL:
				return "<<";
			case Opcodes.LSHR:
				return ">>";
			case Opcodes.LUSHR:
				return ">>>";
			case Opcodes.LAND:
				return "&";
			case Opcodes.LOR:
				return "|";
			case Opcodes.LXOR:
				return "^";

			case Opcodes.I2B:
				return "(byte)";
			case Opcodes.I2S:
				return "(short)";
			case Opcodes.L2I:
			case Opcodes.F2I:
			case Opcodes.D2I:
				return "(int)";
			case Opcodes.I2L:
			case Opcodes.F2L:
			case Opcodes.D2L:
				return "(long)";
			case Opcodes.I2F:
			case Opcodes.L2F:
			case Opcodes.D2F:
				return "(float)";
			case Opcodes.I2D:
			case Opcodes.L2D:
			case Opcodes.F2D:
				return "(double)";
			case Opcodes.I2C:
				return "(char)";

			case Opcodes.INEG:
			case Opcodes.LNEG:
			case Opcodes.FNEG:
			case Opcodes.DNEG:
				return "-";

			default: {
				return getOpcodeName(opcode);
			}
		}
	}

	private static void appendAsmStackInfoArgs(StringBuilder sb, AsmStackInfo[] args) {
		sb.append('(');
		for (int i = 0; i < args.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			appendAsmStackInfo(sb, args[i], "");
		}
		sb.append(')');
	}

	public static void appendThrowableStackTrace(StringBuilder sb, Throwable rc) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
			rc.printStackTrace(pw);
		}
		try {
			sb.append(baos.toString("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			//shouldn't happen
			throw new RuntimeException(e);
		}
	}

	public static Class<?> getExecutableEffectiveReturnType(Executable e) {
		if (e == null) {
			return null;
		}
		if (e instanceof Method) {
			return ((Method) e).getReturnType();
		}
		if (e instanceof Constructor<?>) {
			return e.getDeclaringClass();
		}
		throw new IllegalArgumentException("Unknown executable: " + e);
	}

	public static InsnList clone(InsnList list) {
		if (list == null) {
			return null;
		}
		Map<LabelNode, LabelNode> labelclones = new HashMap<>();
		InsnList result = new InsnList();
		for (AbstractInsnNode ins = list.getFirst(); ins != null; ins = ins.getNext()) {
			result.add(ins.clone(labelclones));
		}
		return result;
	}

	public static Type getInstructionResultAsmType(AbstractInsnNode ins) {
		if (ins == null) {
			return null;
		}
		int opcode = ins.getOpcode();
		switch (opcode) {
			case Opcodes.LDC: {
				LdcInsnNode ldc = (LdcInsnNode) ins;
				return Type.getType(ldc.cst.getClass());
			}
			case Opcodes.BIPUSH:
				return Type.BYTE_TYPE;
			case Opcodes.SIPUSH:
				return Type.SHORT_TYPE;
			case Opcodes.ICONST_M1:
			case Opcodes.ICONST_0:
			case Opcodes.ICONST_1:
			case Opcodes.ICONST_2:
			case Opcodes.ICONST_3:
			case Opcodes.ICONST_4:
			case Opcodes.ICONST_5:
				return Type.INT_TYPE;
			case Opcodes.LCONST_0:
			case Opcodes.LCONST_1:
				return Type.LONG_TYPE;
			case Opcodes.FCONST_0:
			case Opcodes.FCONST_1:
			case Opcodes.FCONST_2:
				return Type.FLOAT_TYPE;
			case Opcodes.DCONST_0:
			case Opcodes.DCONST_1:
				return Type.DOUBLE_TYPE;
			case Opcodes.ACONST_NULL:
				return null;

			case Opcodes.CHECKCAST: {
				TypeInsnNode typeins = (TypeInsnNode) ins;
				return Type.getObjectType(typeins.desc);
			}

			case Opcodes.I2B:
				return Type.BYTE_TYPE;
			case Opcodes.I2C:
				return Type.CHAR_TYPE;
			case Opcodes.I2S:
				return Type.SHORT_TYPE;

			case Opcodes.D2I:
			case Opcodes.F2I:
			case Opcodes.L2I:
				return Type.INT_TYPE;
			case Opcodes.D2L:
			case Opcodes.F2L:
			case Opcodes.I2L:
				return Type.LONG_TYPE;

			case Opcodes.I2F:
			case Opcodes.L2F:
			case Opcodes.D2F:
			case Opcodes.FNEG:
				return Type.FLOAT_TYPE;
			case Opcodes.I2D:
			case Opcodes.L2D:
			case Opcodes.F2D:
			case Opcodes.DNEG:
				return Type.DOUBLE_TYPE;

			case Opcodes.INEG:
			case Opcodes.IADD:
			case Opcodes.ISUB:
			case Opcodes.IMUL:
			case Opcodes.IDIV:
			case Opcodes.IREM:
			case Opcodes.ISHL:
			case Opcodes.ISHR:
			case Opcodes.IUSHR:
			case Opcodes.IAND:
			case Opcodes.IOR:
			case Opcodes.IXOR:
				return Type.INT_TYPE;

			case Opcodes.LNEG:
			case Opcodes.LADD:
			case Opcodes.LSUB:
			case Opcodes.LMUL:
			case Opcodes.LDIV:
			case Opcodes.LREM:
			case Opcodes.LSHL:
			case Opcodes.LSHR:
			case Opcodes.LUSHR:
			case Opcodes.LAND:
			case Opcodes.LOR:
			case Opcodes.LXOR:
				return Type.LONG_TYPE;

			case Opcodes.ARRAYLENGTH:
				return Type.INT_TYPE;
			case Opcodes.NEWARRAY: {
				IntInsnNode intins = (IntInsnNode) ins;

				switch (intins.operand) {
					case Opcodes.T_BOOLEAN:
						return Type.getType(boolean[].class);
					case Opcodes.T_CHAR:
						return Type.getType(char[].class);
					case Opcodes.T_BYTE:
						return Type.getType(byte[].class);
					case Opcodes.T_SHORT:
						return Type.getType(short[].class);
					case Opcodes.T_INT:
						return Type.getType(int[].class);
					case Opcodes.T_LONG:
						return Type.getType(long[].class);
					case Opcodes.T_FLOAT:
						return Type.getType(float[].class);
					case Opcodes.T_DOUBLE:
						return Type.getType(double[].class);
					default:
						throw new IllegalArgumentException("Unknown NEWARRAY operand: " + intins.operand);
				}
			}

			case Opcodes.ANEWARRAY: {
				TypeInsnNode typeins = (TypeInsnNode) ins;
				return Type.getType('[' + Type.getObjectType(typeins.desc).getDescriptor());
			}

			case Opcodes.BASTORE:
			case Opcodes.SASTORE:
			case Opcodes.IASTORE:
			case Opcodes.LASTORE:
			case Opcodes.FASTORE:
			case Opcodes.DASTORE:
			case Opcodes.CASTORE:
			case Opcodes.AASTORE:
				//these dont return anything back to the stack
				return Type.VOID_TYPE;

			case Opcodes.BALOAD:
				return Type.BYTE_TYPE;
			case Opcodes.SALOAD:
				return Type.SHORT_TYPE;
			case Opcodes.IALOAD:
				return Type.INT_TYPE;
			case Opcodes.LALOAD:
				return Type.LONG_TYPE;
			case Opcodes.FALOAD:
				return Type.FLOAT_TYPE;
			case Opcodes.DALOAD:
				return Type.DOUBLE_TYPE;
			case Opcodes.CALOAD:
				return Type.CHAR_TYPE;
			case Opcodes.AALOAD:
				return Type.getType(Object.class); // can't to better here

			case Opcodes.INVOKEVIRTUAL:
			case Opcodes.INVOKEINTERFACE:
			case Opcodes.INVOKESTATIC:
			case Opcodes.INVOKESPECIAL: {
				MethodInsnNode methodins = (MethodInsnNode) ins;

				Type rettype = Type.getReturnType(methodins.desc);
				if (rettype.getSort() == Type.VOID) {
					if (opcode == Opcodes.INVOKESPECIAL) {
						//update the return type to the constructor declaring class
						//so the deconstruction is appropriate
						rettype = Type.getObjectType(methodins.owner);
					}
				}
				return rettype;
			}
			case Opcodes.INVOKEDYNAMIC: {
				InvokeDynamicInsnNode dynins = (InvokeDynamicInsnNode) ins;
				return Type.getReturnType(dynins.desc);
			}

			case Opcodes.GETSTATIC:
			case Opcodes.GETFIELD: {
				FieldInsnNode fieldins = (FieldInsnNode) ins;
				return Type.getType(fieldins.desc);
			}
			default: {
				return null;
			}
		}
	}
}
