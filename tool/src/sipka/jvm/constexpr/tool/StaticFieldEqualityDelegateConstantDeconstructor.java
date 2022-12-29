package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

/**
 * {@link ConstantDeconstructor} that searches the given type with an equal static field to the inlined value. If found,
 * then a {@link Opcodes#GETSTATIC GETSTATIC} instruction is used to retrieve that field. Otherwise a delegate
 * {@link ConstantDeconstructor} is called.
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
			Object value) {
		DeconstructionResult fielddeconstruct = tryDeconstructEqualStaticField(transclass, type, value, fieldNames);
		if (fielddeconstruct != null) {
			return fielddeconstruct;
		}

		if (delegate == null) {
			return null;
		}

		return delegate.deconstructValue(context, transclass, value);
	}

	private static DeconstructionResult tryDeconstructEqualStaticField(TransformedClass transclass, Class<?> type,
			Object val, String... fields) {
		for (Field field : type.getDeclaredFields()) {
			String reflectedfieldname = field.getName();
			for (String fieldname : fields) {
				if (!reflectedfieldname.equals(fieldname)) {
					continue;
				}
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
		}
		return null;
	}
}