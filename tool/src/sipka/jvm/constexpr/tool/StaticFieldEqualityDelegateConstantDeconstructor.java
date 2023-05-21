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
 * then a {@link Opcodes#GETSTATIC GETSTATIC} instruction is used to retrieve that field. Otherwise a delegate
 * {@link ConstantDeconstructor} is called.
 * <p>
 * Fields are expected to have the same type as the declaring type.
 */
final class StaticFieldEqualityDelegateConstantDeconstructor implements ConstantDeconstructor {
	private final ConstantDeconstructor delegate;
	private final Class<?> type;
	private final String[] fieldNames;

	public StaticFieldEqualityDelegateConstantDeconstructor(ConstantDeconstructor delegate, Class<?> type,
			String... fieldNames) {
		this.delegate = delegate;
		this.type = type;
		this.fieldNames = fieldNames;
	}

	public StaticFieldEqualityDelegateConstantDeconstructor withField(String fieldname) {
		String[] narray = Arrays.copyOf(fieldNames, fieldNames.length + 1);
		narray[fieldNames.length] = fieldname;
		return new StaticFieldEqualityDelegateConstantDeconstructor(delegate, type, narray);
	}

	public ConstantDeconstructor getDelegate() {
		return delegate;
	}

	public Class<?> getType() {
		return type;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		DeconstructionResult fielddeconstruct = tryDeconstructEqualStaticField(context, transclass, type, value,
				fieldNames);
		if (fielddeconstruct != null) {
			return fielddeconstruct;
		}

		if (delegate == null) {
			return null;
		}

		return delegate.deconstructValue(context, transclass, methodnode, value);
	}

	private static DeconstructionResult tryDeconstructEqualStaticField(ConstantExpressionInliner context,
			TransformedClass transclass, Class<?> type, Object value, String[] fields) {
		Field[] declaredfields = type.getDeclaredFields();
		for (String fieldname : fields) {
			boolean foundwithname = false;
			for (Field field : declaredfields) {
				String reflectedfieldname = field.getName();
				if (!reflectedfieldname.equals(fieldname)) {
					continue;
				}
				Class<?> fieldtype = field.getType();
				if (!fieldtype.equals(type)) {
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
					Type fieldasmtype = Type.getType(fieldtype);
					context.logConfigClassMemberInaccessible(fieldasmtype.getInternalName(), fieldname,
							fieldasmtype.getDescriptor(), e);
					continue;
				}
				Type fieldasmtype = Type.getType(fieldtype);

				InsnList result = new InsnList();
				result.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(type), fieldname,
						fieldasmtype.getDescriptor()));
				return DeconstructionResult.createField(result, Type.getType(type), fieldname, fieldasmtype);
			}
			if (!foundwithname) {
				Type typeasmtype = Type.getType(type);
				context.logConfigClassMemberInaccessible(typeasmtype.getInternalName(), fieldname,
						typeasmtype.getDescriptor(), new NoSuchFieldException(fieldname));
			}
		}
		return null;
	}
}