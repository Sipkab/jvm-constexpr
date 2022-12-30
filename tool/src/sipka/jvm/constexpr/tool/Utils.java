package sipka.jvm.constexpr.tool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import sipka.jvm.constexpr.tool.AsmStackInfo.Kind;
import sipka.jvm.constexpr.tool.log.BytecodeLocation;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.IntInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LabelNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LineNumberNode;
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
	private static final String[] OPCODE_NAMES = new String[200];
	static {
		OPCODE_NAMES[0] = "NOP";
		OPCODE_NAMES[1] = "ACONST_NULL";
		OPCODE_NAMES[2] = "ICONST_M1";
		OPCODE_NAMES[3] = "ICONST_0";
		OPCODE_NAMES[4] = "ICONST_1";
		OPCODE_NAMES[5] = "ICONST_2";
		OPCODE_NAMES[6] = "ICONST_3";
		OPCODE_NAMES[7] = "ICONST_4";
		OPCODE_NAMES[8] = "ICONST_5";
		OPCODE_NAMES[9] = "LCONST_0";
		OPCODE_NAMES[10] = "LCONST_1";
		OPCODE_NAMES[11] = "FCONST_0";
		OPCODE_NAMES[12] = "FCONST_1";
		OPCODE_NAMES[13] = "FCONST_2";
		OPCODE_NAMES[14] = "DCONST_0";
		OPCODE_NAMES[15] = "DCONST_1";
		OPCODE_NAMES[16] = "BIPUSH";
		OPCODE_NAMES[17] = "SIPUSH";
		OPCODE_NAMES[18] = "LDC";
		OPCODE_NAMES[21] = "ILOAD";
		OPCODE_NAMES[22] = "LLOAD";
		OPCODE_NAMES[23] = "FLOAD";
		OPCODE_NAMES[24] = "DLOAD";
		OPCODE_NAMES[25] = "ALOAD";
		OPCODE_NAMES[46] = "IALOAD";
		OPCODE_NAMES[47] = "LALOAD";
		OPCODE_NAMES[48] = "FALOAD";
		OPCODE_NAMES[49] = "DALOAD";
		OPCODE_NAMES[50] = "AALOAD";
		OPCODE_NAMES[51] = "BALOAD";
		OPCODE_NAMES[52] = "CALOAD";
		OPCODE_NAMES[53] = "SALOAD";
		OPCODE_NAMES[54] = "ISTORE";
		OPCODE_NAMES[55] = "LSTORE";
		OPCODE_NAMES[56] = "FSTORE";
		OPCODE_NAMES[57] = "DSTORE";
		OPCODE_NAMES[58] = "ASTORE";
		OPCODE_NAMES[79] = "IASTORE";
		OPCODE_NAMES[80] = "LASTORE";
		OPCODE_NAMES[81] = "FASTORE";
		OPCODE_NAMES[82] = "DASTORE";
		OPCODE_NAMES[83] = "AASTORE";
		OPCODE_NAMES[84] = "BASTORE";
		OPCODE_NAMES[85] = "CASTORE";
		OPCODE_NAMES[86] = "SASTORE";
		OPCODE_NAMES[87] = "POP";
		OPCODE_NAMES[88] = "POP2";
		OPCODE_NAMES[89] = "DUP";
		OPCODE_NAMES[90] = "DUP_X1";
		OPCODE_NAMES[91] = "DUP_X2";
		OPCODE_NAMES[92] = "DUP2";
		OPCODE_NAMES[93] = "DUP2_X1";
		OPCODE_NAMES[94] = "DUP2_X2";
		OPCODE_NAMES[95] = "SWAP";
		OPCODE_NAMES[96] = "IADD";
		OPCODE_NAMES[97] = "LADD";
		OPCODE_NAMES[98] = "FADD";
		OPCODE_NAMES[99] = "DADD";
		OPCODE_NAMES[100] = "ISUB";
		OPCODE_NAMES[101] = "LSUB";
		OPCODE_NAMES[102] = "FSUB";
		OPCODE_NAMES[103] = "DSUB";
		OPCODE_NAMES[104] = "IMUL";
		OPCODE_NAMES[105] = "LMUL";
		OPCODE_NAMES[106] = "FMUL";
		OPCODE_NAMES[107] = "DMUL";
		OPCODE_NAMES[108] = "IDIV";
		OPCODE_NAMES[109] = "LDIV";
		OPCODE_NAMES[110] = "FDIV";
		OPCODE_NAMES[111] = "DDIV";
		OPCODE_NAMES[112] = "IREM";
		OPCODE_NAMES[113] = "LREM";
		OPCODE_NAMES[114] = "FREM";
		OPCODE_NAMES[115] = "DREM";
		OPCODE_NAMES[116] = "INEG";
		OPCODE_NAMES[117] = "LNEG";
		OPCODE_NAMES[118] = "FNEG";
		OPCODE_NAMES[119] = "DNEG";
		OPCODE_NAMES[120] = "ISHL";
		OPCODE_NAMES[121] = "LSHL";
		OPCODE_NAMES[122] = "ISHR";
		OPCODE_NAMES[123] = "LSHR";
		OPCODE_NAMES[124] = "IUSHR";
		OPCODE_NAMES[125] = "LUSHR";
		OPCODE_NAMES[126] = "IAND";
		OPCODE_NAMES[127] = "LAND";
		OPCODE_NAMES[128] = "IOR";
		OPCODE_NAMES[129] = "LOR";
		OPCODE_NAMES[130] = "IXOR";
		OPCODE_NAMES[131] = "LXOR";
		OPCODE_NAMES[132] = "IINC";
		OPCODE_NAMES[133] = "I2L";
		OPCODE_NAMES[134] = "I2F";
		OPCODE_NAMES[135] = "I2D";
		OPCODE_NAMES[136] = "L2I";
		OPCODE_NAMES[137] = "L2F";
		OPCODE_NAMES[138] = "L2D";
		OPCODE_NAMES[139] = "F2I";
		OPCODE_NAMES[140] = "F2L";
		OPCODE_NAMES[141] = "F2D";
		OPCODE_NAMES[142] = "D2I";
		OPCODE_NAMES[143] = "D2L";
		OPCODE_NAMES[144] = "D2F";
		OPCODE_NAMES[145] = "I2B";
		OPCODE_NAMES[146] = "I2C";
		OPCODE_NAMES[147] = "I2S";
		OPCODE_NAMES[148] = "LCMP";
		OPCODE_NAMES[149] = "FCMPL";
		OPCODE_NAMES[150] = "FCMPG";
		OPCODE_NAMES[151] = "DCMPL";
		OPCODE_NAMES[152] = "DCMPG";
		OPCODE_NAMES[153] = "IFEQ";
		OPCODE_NAMES[154] = "IFNE";
		OPCODE_NAMES[155] = "IFLT";
		OPCODE_NAMES[156] = "IFGE";
		OPCODE_NAMES[157] = "IFGT";
		OPCODE_NAMES[158] = "IFLE";
		OPCODE_NAMES[159] = "IF_ICMPEQ";
		OPCODE_NAMES[160] = "IF_ICMPNE";
		OPCODE_NAMES[161] = "IF_ICMPLT";
		OPCODE_NAMES[162] = "IF_ICMPGE";
		OPCODE_NAMES[163] = "IF_ICMPGT";
		OPCODE_NAMES[164] = "IF_ICMPLE";
		OPCODE_NAMES[165] = "IF_ACMPEQ";
		OPCODE_NAMES[166] = "IF_ACMPNE";
		OPCODE_NAMES[167] = "GOTO";
		OPCODE_NAMES[168] = "JSR";
		OPCODE_NAMES[169] = "RET";
		OPCODE_NAMES[170] = "TABLESWITCH";
		OPCODE_NAMES[171] = "LOOKUPSWITCH";
		OPCODE_NAMES[172] = "IRETURN";
		OPCODE_NAMES[173] = "LRETURN";
		OPCODE_NAMES[174] = "FRETURN";
		OPCODE_NAMES[175] = "DRETURN";
		OPCODE_NAMES[176] = "ARETURN";
		OPCODE_NAMES[177] = "RETURN";
		OPCODE_NAMES[178] = "GETSTATIC";
		OPCODE_NAMES[179] = "PUTSTATIC";
		OPCODE_NAMES[180] = "GETFIELD";
		OPCODE_NAMES[181] = "PUTFIELD";
		OPCODE_NAMES[182] = "INVOKEVIRTUAL";
		OPCODE_NAMES[183] = "INVOKESPECIAL";
		OPCODE_NAMES[184] = "INVOKESTATIC";
		OPCODE_NAMES[185] = "INVOKEINTERFACE";
		OPCODE_NAMES[186] = "INVOKEDYNAMIC";
		OPCODE_NAMES[187] = "NEW";
		OPCODE_NAMES[188] = "NEWARRAY";
		OPCODE_NAMES[189] = "ANEWARRAY";
		OPCODE_NAMES[190] = "ARRAYLENGTH";
		OPCODE_NAMES[191] = "ATHROW";
		OPCODE_NAMES[192] = "CHECKCAST";
		OPCODE_NAMES[193] = "INSTANCEOF";
		OPCODE_NAMES[194] = "MONITORENTER";
		OPCODE_NAMES[195] = "MONITOREXIT";
		OPCODE_NAMES[197] = "MULTIANEWARRAY";
		OPCODE_NAMES[198] = "IFNULL";
		OPCODE_NAMES[199] = "IFNONNULL";
	}

	public static String getOpcodeName(int opcode) {
		try {
			return OPCODE_NAMES[opcode];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw e;
		}
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
			if (typeinternalname.equals(owner)) {
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
		if (value instanceof Byte)
			return String.format("(byte)0x%02x", (byte) value);
		if (value instanceof Short)
			return String.format("(short)%d", (short) value);
		if (value instanceof Long)
			return ((long) value) + "L";
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
		if (value instanceof Integer || value instanceof Boolean)
			return value.toString();
		return value.toString();
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
			case NULL:
				sb.append("null");
				return;
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
					sb.append(" { ");
					for (int i = 0; i < elements.length; i++) {
						if (i != 0) {
							sb.append(", ");
						}
						appendAsmArrayElement(sb, componenttype, elements[i]);
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
			default: {
				sb.append(info);
				break;
			}
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
}
