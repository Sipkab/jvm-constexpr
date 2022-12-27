package sipka.jvm.constexpr.tool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

/**
 * {@link ConstantReconstructor} that reconstructs the object by calling its specified constructor.
 */
final class ConstructorBasedConstantReconstructor implements ConstantReconstructor {
	private final Constructor<?> constructor;

	private transient final String typeInternalName;
	private transient final Class<?>[] parameterTypes;

	public ConstructorBasedConstantReconstructor(Constructor<?> constructor) {
		this.parameterTypes = constructor.getParameterTypes();
		this.typeInternalName = Type.getInternalName(constructor.getDeclaringClass());
		this.constructor = constructor;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins) {
		//ins is the INVOKESPECIAL
		int paramcount = parameterTypes.length;
		Object[] args = new Object[paramcount];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[paramcount];
		if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins, args,
				derivedargs)) {
			return null;
		}
		AbstractInsnNode beforeins = (paramcount == 0 ? ins : derivedargs[0].getFirstIns()).getPrevious();
		//expected 
		//	new java.lang.Object
		//	dup
		//opcodes
		if (beforeins == null || beforeins.getOpcode() != Opcodes.DUP) {
			return null;
		}
		AbstractInsnNode beforedup = beforeins.getPrevious();
		if (beforedup == null || beforedup.getOpcode() != Opcodes.NEW) {
			return null;
		}
		TypeInsnNode typeins = (TypeInsnNode) beforedup;
		if (!typeInternalName.equals(typeins.desc)) {
			//TODO log?
			return null;
		}

		Object instance;
		try {
			instance = constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return new AsmStackReconstructedValue(typeins, ins.getNext(), instance);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[constructor=");
		builder.append(constructor);
		builder.append("]");
		return builder.toString();
	}
}