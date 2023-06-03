package sipka.jvm.constexpr.tool;

import java.lang.reflect.Constructor;

import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.TypeInsnNode;

/**
 * {@link ConstantReconstructor} that reconstructs the object by calling its specified constructor.
 */
final class ConstructorBasedConstantReconstructor implements ConstantReconstructor {
	private final Constructor<?> constructor;
	private final ReconstructorPredicate predicate;

	private transient final String typeInternalName;
	private transient final Class<?>[] parameterTypes;

	public ConstructorBasedConstantReconstructor(Constructor<?> constructor, ReconstructorPredicate predicate) {
		this.constructor = constructor;
		this.predicate = predicate;
		this.parameterTypes = constructor.getParameterTypes();
		this.typeInternalName = Type.getInternalName(constructor.getDeclaringClass());
	}

	public ConstructorBasedConstantReconstructor(Constructor<?> constructor) {
		this(constructor, ReconstructorPredicate.ALLOW_ALL);
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		//ins is the INVOKESPECIAL
		int paramcount = parameterTypes.length;
		Object[] args = new Object[paramcount];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[paramcount];
		try {
			if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins,
					args, derivedargs)) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newMethodArgumentsReconstructionException(e, ins, typeInternalName,
					Utils.CONSTRUCTOR_METHOD_NAME, Type.getConstructorDescriptor(constructor));
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
			throw new IllegalArgumentException("Attempting to reconstruct different type: " + typeInternalName
					+ " with NEW type on stack: " + typeins.desc);
		}

		if (!predicate.canReconstruct(null, constructor, args)) {
			context.getInliner().logReconstructionNotAllowed(null, constructor, args);
			return null;
		}

		Object instance;
		try {
			instance = constructor.newInstance(args);
		} catch (Exception e) {
			throw context.newMethodInvocationFailureReconstructionException(e, ins, typeInternalName,
					Utils.CONSTRUCTOR_METHOD_NAME, Type.getConstructorDescriptor(constructor), null, args);
		}
		return new AsmStackReconstructedValue(typeins, ins.getNext(),
				AsmStackInfo.createConstructor(Type.getObjectType(typeInternalName), Type.getType(constructor),
						AsmStackReconstructedValue.toStackInfoArray(derivedargs)),
				instance);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[constructor=");
		builder.append(constructor);
		builder.append(", predicate=");
		builder.append(predicate);
		builder.append("]");
		return builder.toString();
	}
}