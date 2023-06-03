package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class FieldBasedConstantReconstructor implements ConstantReconstructor {
	private final Field field;
	private final ReconstructorPredicate predicate;

	public FieldBasedConstantReconstructor(Field field, ReconstructorPredicate predicate) {
		this.field = field;
		this.predicate = predicate;
	}

	public FieldBasedConstantReconstructor(Field field) {
		this(field, Modifier.isStatic(field.getModifiers()) ? ReconstructorPredicate.ALLOW_ALL
				: ReconstructorPredicate.ALLOW_INSTANCE_OF);
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		if (ins.getOpcode() == Opcodes.GETSTATIC) {
			//no need to call the reconstructor predicate here
			//as if a static field doesn't need to be reconstructed, then don't configure it
			//that is, a reconstructor predicate for a static field should always return true
			Object fieldval;
			try {
				fieldval = field.get(null);
			} catch (Exception e) {
				throw context.newFieldAccessFailureReconstructionException(e, ins,
						Type.getInternalName(field.getDeclaringClass()), field.getName(),
						Type.getDescriptor(field.getType()), null);
			}
			return new AsmStackReconstructedValue(ins, ins.getNext(), AsmStackInfo.createStaticField(
					Type.getType(field.getDeclaringClass()), field.getName(), Type.getType(field.getType())), fieldval);
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

		Object obj = instanceval.getValue();
		if (!predicate.canReconstruct(obj, field, null)) {
			context.getInliner().logReconstructionNotAllowed(obj, field, null);
			return null;
		}
		Object fieldval;
		try {
			fieldval = field.get(obj);
		} catch (Exception e) {
			throw context.newFieldAccessFailureReconstructionException(e, ins,
					Type.getInternalName(field.getDeclaringClass()), field.getName(),
					Type.getDescriptor(field.getType()), instanceval);
		}
		return new AsmStackReconstructedValue(instanceval.getFirstIns(), ins.getNext(),
				AsmStackInfo.createField(Type.getType(field.getDeclaringClass()), field.getName(),
						Type.getType(field.getType()), instanceval.getStackInfo()),
				fieldval);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[field=");
		builder.append(field);
		builder.append(", predicate=");
		builder.append(predicate);
		builder.append("]");
		return builder.toString();
	}

}
