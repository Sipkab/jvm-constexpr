package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} that places a {@link Opcodes#GETSTATIC GETSTATIC} instruction for the inlined enum
 * value.
 * <p>
 * The enum type is determined based on the inlined value.
 */
final class EnumFieldConstantDeconstructor implements ConstantDeconstructor {
	public static final EnumFieldConstantDeconstructor INSTANCE = new EnumFieldConstantDeconstructor();

	private EnumFieldConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		Enum<?> en = (Enum<?>) value;
		String fieldname = en.name();

		Class<?> enumclass = en.getDeclaringClass();

		Type enumclasstype = Type.getType(enumclass);
		InsnList result = new InsnList();
		result.add(new FieldInsnNode(Opcodes.GETSTATIC, enumclasstype.getInternalName(), fieldname,
				enumclasstype.getDescriptor()));
		return DeconstructionResult.createField(result, enumclasstype, fieldname, enumclasstype);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}