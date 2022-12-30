package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

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
			TransformedClass transclass, Class<?> type, Object val, String[] fields) {
		Field[] declaredfields = type.getDeclaredFields();
		for (String fieldname : fields) {
			boolean foundwithname = false;
			for (Field field : declaredfields) {
				String reflectedfieldname = field.getName();
				if (!reflectedfieldname.equals(fieldname)) {
					continue;
				}
				if (!field.getType().equals(type)) {
					//different field type than expected
					continue;
				}
				foundwithname = true;
				try {
					if (val.equals(field.get(null))) {
						Class<?> fieldtype = field.getType();
						Type fieldasmtype = Type.getType(fieldtype);

						InsnList result = new InsnList();
						result.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(type), fieldname,
								fieldasmtype.getDescriptor()));
						return DeconstructionResult.createField(result, Type.getType(type), fieldname, fieldasmtype);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}
			if (!foundwithname) {
				Type typeasmtype = Type.getType(type);
				context.logConfigClassMemberNotAvailable(typeasmtype.getInternalName(), fieldname,
						typeasmtype.getDescriptor(), null);
			}
		}
		return null;
	}
}