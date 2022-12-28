package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class FieldBasedConstantReconstructor implements ConstantReconstructor {
	private final Field field;

	public FieldBasedConstantReconstructor(Field field) {
		this.field = field;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		if (ins.getOpcode() == Opcodes.GETSTATIC) {
			Object fieldval;
			try {
				fieldval = field.get(null);
			} catch (Exception e) {
				throw context.newFieldAccessFailureReconstructionException(e, ins,
						Type.getInternalName(field.getDeclaringClass()), field.getName(),
						Type.getDescriptor(field.getType()), null);
			}
			return new AsmStackReconstructedValue(ins, ins.getNext(), fieldval);
		}
		//get from an instance
		AsmStackReconstructedValue instanceval;
		try {
			instanceval = context.getInliner()
					.reconstructStackValue(context.withReceiverType(field.getDeclaringClass()), ins.getPrevious());
		} catch (ReconstructionException e) {
			throw context.newInstanceAccessFailureReconstructionException(e, ins,
					Type.getInternalName(field.getDeclaringClass()), field.getName(),
					Type.getDescriptor(field.getType()));
		}
		if (instanceval == null) {
			return null;
		}
		Object fieldval;
		try {
			fieldval = field.get(instanceval.getValue());
		} catch (Exception e) {
			throw context.newFieldAccessFailureReconstructionException(e, ins,
					Type.getInternalName(field.getDeclaringClass()), field.getName(),
					Type.getDescriptor(field.getType()), instanceval);
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
