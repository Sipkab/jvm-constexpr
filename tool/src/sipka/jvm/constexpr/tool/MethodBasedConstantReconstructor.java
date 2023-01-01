package sipka.jvm.constexpr.tool;

import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

/**
 * {@link ConstantReconstructor} that calls a static or instance method to reconstruct a value.
 */
final class MethodBasedConstantReconstructor implements ConstantReconstructor {
	public static final MethodBasedConstantReconstructor TOSTRING_INSTANCE;
	static {
		try {
			TOSTRING_INSTANCE = new MethodBasedConstantReconstructor(Object.class.getMethod("toString"));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError("Object.toString method not found.", e);
		}
	}

	private final Method method;

	private final transient Class<?>[] parameterTypes;

	public MethodBasedConstantReconstructor(Method m) {
		this.parameterTypes = m.getParameterTypes();
		this.method = m;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		//ins is the INVOKESTATIC/INVOKEVIRTUAL instruction
		int paramcount = parameterTypes.length;
		Object[] args = new Object[paramcount];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[paramcount];
		try {
			if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins,
					args, derivedargs)) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newMethodArgumentsReconstructionException(e, ins,
					Type.getInternalName(method.getDeclaringClass()), method.getName(),
					Type.getMethodDescriptor(method));
		}
		AbstractInsnNode firstins = paramcount == 0 ? ins : derivedargs[0].getFirstIns();
		Object subject;
		AsmStackReconstructedValue subjectval;
		boolean staticfunc = ins.getOpcode() == Opcodes.INVOKESTATIC;
		if (staticfunc) {
			subjectval = null;
			subject = null;
		} else {
			try {
				subjectval = context.getInliner().reconstructStackValue(
						context.withReceiverType(method.getDeclaringClass()), firstins.getPrevious());
				if (subjectval == null) {
					return null;
				}
				firstins = subjectval.getFirstIns();
				subject = subjectval.getValue();
			} catch (ReconstructionException e) {
				throw context.newInstanceAccessFailureReconstructionException(e, ins,
						Type.getInternalName(method.getDeclaringClass()), method.getName(),
						Type.getMethodDescriptor(method));
			}
		}
		Object resultobj;
		try {
			resultobj = method.invoke(subject, args);
		} catch (Exception e) {
			throw context.newMethodInvocationFailureReconstructionException(e, ins,
					Type.getInternalName(method.getDeclaringClass()), method.getName(),
					Type.getMethodDescriptor(method), subject, args);
		}
		AsmStackInfo stackinfo;
		if (staticfunc) {
			stackinfo = AsmStackInfo.createStaticMethod(Type.getType(method.getDeclaringClass()), method.getName(),
					Type.getType(method), AsmStackReconstructedValue.toStackInfoArray(derivedargs));
		} else {
			stackinfo = AsmStackInfo.createMethod(Type.getType(method.getDeclaringClass()), method.getName(),
					Type.getType(method), subjectval.getStackInfo(),
					AsmStackReconstructedValue.toStackInfoArray(derivedargs));
		}
		return new AsmStackReconstructedValue(firstins, ins.getNext(), stackinfo, resultobj);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[method=");
		builder.append(method);
		builder.append("]");
		return builder.toString();
	}
}