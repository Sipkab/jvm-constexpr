package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldInsnNode;

class DynamicInstanceFieldBasedConstantReconstructor implements ConstantReconstructor {
	public static final DynamicInstanceFieldBasedConstantReconstructor INSTANCE = new DynamicInstanceFieldBasedConstantReconstructor();

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		FieldInsnNode fieldins = (FieldInsnNode) ins;
		//ins is GETFIELD
		AsmStackReconstructedValue instanceval;
		try {
			instanceval = context.getInliner().reconstructStackValue(context.withReceiverType(null), ins.getPrevious());
		} catch (ReconstructionException e) {
			throw context.newInstanceAccessFailureReconstructionException(e, ins, fieldins.owner, fieldins.name,
					fieldins.desc);
		}
		if (instanceval == null) {
			return null;
		}
		Object val = instanceval.getValue();
		if (val == null) {
			return null;
		}
		List<NoSuchFieldException> notfoundexc = null;
		boolean foundowner = false;
		for (Class<?> c = val.getClass(); c != null; c = c.getSuperclass()) {
			if (!foundowner) {
				if (fieldins.owner.equals(Type.getInternalName(c))) {
					foundowner = true;
				} else {
					//owner not yet found, dont attempt to get the field yet
					continue;
				}
			}
			Object fieldval;
			Field field;
			try {
				field = Utils.getFieldForDescriptor(c, fieldins.name, fieldins.desc);
			} catch (NoSuchFieldException e) {
				if (notfoundexc == null) {
					notfoundexc = new ArrayList<>();
				}
				notfoundexc.add(e);
				continue;
			}
			try {
				field.setAccessible(true);
				fieldval = field.get(instanceval.getValue());
			} catch (Exception e) {
				throw context.newFieldAccessFailureReconstructionException(e, ins, fieldins.owner, fieldins.name,
						fieldins.desc, instanceval);
			}
			return new AsmStackReconstructedValue(instanceval.getFirstIns(), ins.getNext(),
					AsmStackInfo.createField(Type.getType(field.getDeclaringClass()), field.getName(),
							Type.getType(field.getType()), instanceval.getStackInfo()),
					fieldval);
		}

		if (notfoundexc == null) {
			notfoundexc = new ArrayList<>();
			//have at least 1 exception for the cause of the ReconstructionException
			notfoundexc.add(new NoSuchFieldException(Type.getType(fieldins.desc).getClassName() + " "
					+ Type.getObjectType(fieldins.owner).getClassName() + "." + fieldins.name));
		}

		Iterator<NoSuchFieldException> excit = notfoundexc.iterator();

		ReconstructionException throwexc = context.newFieldNotFoundReconstructionException(excit.next(), ins,
				fieldins.owner, fieldins.name, fieldins.desc);
		while (excit.hasNext()) {
			throwexc.addSuppressed(excit.next());
		}
		throw throwexc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}

}
