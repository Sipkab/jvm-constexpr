package sipka.jvm.constexpr.tool;

import java.lang.reflect.Array;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.IntInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.LdcInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

/**
 * {@link ConstantDeconstructor} that deconstructs the value into an array.
 */
class ArrayConstantDeconstructor implements ConstantDeconstructor {
	public static final ArrayConstantDeconstructor INSTANCE = new ArrayConstantDeconstructor();

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		if (value == null) {
			//shouldn't happen
			throw new NullPointerException("Attempting to deconstruct null value as an array.");
		}
		Class<?> componenttype = value.getClass().getComponentType();
		if (componenttype == null) {
			//shouldn't happen either
			throw new NullPointerException("Attempting to deconstruct non array type as array: " + value.getClass());
		}
		InsnList result = new InsnList();
		int length = Array.getLength(value);
		AsmStackInfo[] elementinfos = new AsmStackInfo[length];

		result.add(new LdcInsnNode(length));

		if (componenttype.isPrimitive()) {
			result.add(new IntInsnNode(Opcodes.NEWARRAY, Utils.getOperandForAsmNewArrayInstruction(componenttype)));
		} else {
			result.add(new TypeInsnNode(Opcodes.ANEWARRAY, Type.getInternalName(componenttype)));
		}

		Type componentasmtype = Type.getType(componenttype);
		int storeopcode = Utils.getOperandForAsmStoreArrayInstruction(componenttype);

		for (int i = 0; i < length; i++) {
			result.add(new InsnNode(Opcodes.DUP));
			result.add(new LdcInsnNode(i));
			DeconstructionResult deconstructed = context.deconstructValue(transclass, Array.get(value, i),
					componentasmtype);
			if (deconstructed == null) {
				//failed to deconstruct this element
				return null;
			}
			result.add(deconstructed.getInstructions());
			result.add(new InsnNode(storeopcode));
			elementinfos[i] = deconstructed.getStackInfo();
		}
		return DeconstructionResult.createArray(result, componentasmtype, AsmStackInfo.createConstant(length),
				elementinfos);
	}

}
