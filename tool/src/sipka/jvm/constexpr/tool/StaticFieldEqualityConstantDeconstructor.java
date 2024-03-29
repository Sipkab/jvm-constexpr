package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;
import java.util.Arrays;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} that searches the given type with an equal static field to the inlined value. If found,
 * then a {@link Opcodes#GETSTATIC GETSTATIC} instruction is used to retrieve that field.
 * <p>
 * Fields are expected to have the same type as the declaring type.
 */
final class StaticFieldEqualityConstantDeconstructor implements ConstantDeconstructor {
	private final Type fieldType;
	private final Class<?> fieldOwnerType;
	private final String[] fieldNames;

	public StaticFieldEqualityConstantDeconstructor(Type fieldtype, Class<?> fieldownertype, String... fieldNames) {
		this.fieldType = fieldtype;
		this.fieldOwnerType = fieldownertype;
		this.fieldNames = fieldNames;
	}

	public StaticFieldEqualityConstantDeconstructor withField(String fieldname) {
		String[] narray = Arrays.copyOf(fieldNames, fieldNames.length + 1);
		narray[fieldNames.length] = fieldname;
		return new StaticFieldEqualityConstantDeconstructor(fieldType, fieldOwnerType, narray);
	}

	public Type getFieldType() {
		return fieldType;
	}

	public Class<?> getFieldOwnerType() {
		return fieldOwnerType;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		return tryDeconstructEqualStaticField(context, transclass, fieldOwnerType, fieldType, value, fieldNames);
	}

	private static DeconstructionResult tryDeconstructEqualStaticField(ConstantExpressionInliner context,
			TransformedClass transclass, Class<?> fieldownertype, Type fieldasmtype, Object value, String[] fields) {
		Field[] declaredfields = fieldownertype.getDeclaredFields();
		for (String fieldname : fields) {
			boolean foundwithname = false;
			for (Field field : declaredfields) {
				if (!field.getName().equals(fieldname)) {
					continue;
				}
				if (!Type.getType(field.getType()).equals(fieldasmtype)) {
					//different field type than expected
					continue;
				}
				foundwithname = true;
				try {
					if (!value.equals(field.get(null))) {
						continue;
					}
					//okay, proceed
				} catch (Exception e) {
					//log the error that we couldn't access the static field
					context.logConfigClassMemberInaccessible(fieldasmtype.getInternalName(), fieldname,
							fieldasmtype.getDescriptor(), e);
					continue;
				}

				InsnList result = new InsnList();
				result.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(fieldownertype), fieldname,
						fieldasmtype.getDescriptor()));
				return DeconstructionResult.createField(result, Type.getType(fieldownertype), fieldname, fieldasmtype);
			}
			if (!foundwithname) {
				Type ownertypeasmtype = Type.getType(fieldownertype);
				context.logConfigClassMemberInaccessible(ownertypeasmtype.getInternalName(), fieldname,
						fieldasmtype.getDescriptor(), new NoSuchFieldException(fieldname));
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[type=");
		builder.append(fieldType);
		builder.append(", fieldNames=");
		builder.append(Arrays.toString(fieldNames));
		builder.append("]");
		return builder.toString();
	}
}