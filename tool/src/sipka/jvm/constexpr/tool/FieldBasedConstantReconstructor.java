package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class FieldBasedConstantReconstructor implements ConstantReconstructor {
	private final Field field;

	public FieldBasedConstantReconstructor(Field field) {
		this.field = field;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		if (ins.getOpcode() == Opcodes.GETSTATIC) {
			Object fieldval;
			try {
				fieldval = field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			return new AsmStackReconstructedValue(ins, ins.getNext(), fieldval);
		}
		//get from an instance
		AsmStackReconstructedValue instanceval = context.getInliner()
				.reconstructStackValue(context.withReceiverType(field.getDeclaringClass()), ins.getPrevious());
		if (instanceval == null) {
			return null;
		}
		Object fieldval;
		try {
			fieldval = field.get(instanceval.getValue());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new AsmStackReconstructedValue(instanceval.getFirstIns(), ins.getNext(), fieldval);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[field=");
		builder.append(field);
		builder.append("]");
		return builder.toString();
	}

}
