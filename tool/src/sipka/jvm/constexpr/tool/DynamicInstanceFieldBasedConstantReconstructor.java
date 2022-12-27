package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class DynamicInstanceFieldBasedConstantReconstructor implements ConstantReconstructor {
	private final String fieldOwnerInternalName;
	private final String fieldName;

	public DynamicInstanceFieldBasedConstantReconstructor(String fieldOwnerInternalName, String fieldName) {
		this.fieldOwnerInternalName = fieldOwnerInternalName;
		this.fieldName = fieldName;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		//ins is GETSTATIC
		AsmStackReconstructedValue instanceval = context.getInliner()
				.reconstructStackValue(context.withReceiverType(null), ins.getPrevious());
		if (instanceval == null) {
			return null;
		}
		Object val = instanceval.getValue();
		if (val == null) {
			return null;
		}
		boolean foundowner = false;
		for (Class<?> c = val.getClass(); c != null; c = c.getSuperclass()) {
			if (!foundowner) {
				if (fieldOwnerInternalName.equals(Type.getInternalName(c))) {
					foundowner = true;
				} else {
					//owner not yet found, dont attempt to get the field yet
					continue;
				}
			}
			Object fieldval;
			try {
				Field field = c.getDeclaredField(fieldName);
				field.setAccessible(true);
				fieldval = field.get(instanceval.getValue());
			} catch (NoSuchFieldException e) {
				// TODO: handle exception
				continue;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			return new AsmStackReconstructedValue(instanceval.getFirstIns(), ins.getNext(), fieldval);
		}
		//TODO log
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[fieldOwnerInternalName=");
		builder.append(fieldOwnerInternalName);
		builder.append(", fieldName=");
		builder.append(fieldName);
		builder.append("]");
		return builder.toString();
	}

}
