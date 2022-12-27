package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

/**
 * {@link ConstantDeconstructor} that places a {@link Opcodes#GETSTATIC GETSTATIC} instruction for the inlined enum
 * value.
 * <p>
 * The enum type is determined based on the inlined value.
 */
final class EnumFieldConstantDeconstructor implements ConstantDeconstructor {
	public static final EnumFieldConstantDeconstructor INSTANCE = new EnumFieldConstantDeconstructor();

	@Override
	public InsnList deconstructValue(ConstantExpressionInliner context, TransformedClass transclass, Object value) {
		Enum<?> en = (Enum<?>) value;
		String fieldname = en.name();

		Class<?> enumclass = en.getDeclaringClass();

		InsnList result = new InsnList();
		result.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(enumclass), fieldname,
				Type.getDescriptor(enumclass)));
		return result;
	}
}