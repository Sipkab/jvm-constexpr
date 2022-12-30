package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} that places a {@link Opcodes#GETSTATIC GETSTATIC} instruction for the inlined value.
 */
final class FieldConstantDeconstructor implements ConstantDeconstructor {
	private final String ownerInternalName;
	private final String fieldName;
	private final String fieldDescriptor;

	private FieldConstantDeconstructor(String ownerInternalName, String fieldName, String fieldDescriptor) {
		this.ownerInternalName = ownerInternalName;
		this.fieldName = fieldName;
		this.fieldDescriptor = fieldDescriptor;
	}

	public static FieldConstantDeconstructor createStaticFieldDeconstructor(String ownerInternalName, String fieldName,
			String fieldDescriptor) {
		return new FieldConstantDeconstructor(ownerInternalName, fieldName, fieldDescriptor);
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		InsnList result = new InsnList();
		result.add(new FieldInsnNode(Opcodes.GETSTATIC, ownerInternalName, fieldName, fieldDescriptor));
		return DeconstructionResult.createField(result, Type.getObjectType(ownerInternalName), fieldName,
				Type.getType(fieldDescriptor));
	}
}