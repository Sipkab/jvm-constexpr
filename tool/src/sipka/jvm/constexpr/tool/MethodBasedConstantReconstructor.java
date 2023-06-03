package sipka.jvm.constexpr.tool;

import java.lang.reflect.Method;

import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * {@link ConstantReconstructor} that calls a static or instance method to reconstruct a value.
 */
final class MethodBasedConstantReconstructor implements ConstantReconstructor {
	private final Method method;
	private final ReconstructorPredicate predicate;

	private final transient Class<?>[] parameterTypes;

	public MethodBasedConstantReconstructor(Method m, ReconstructorPredicate predicate) {
		this.method = m;
		this.predicate = predicate;
		this.parameterTypes = m.getParameterTypes();
	}

	public Method getMethod() {
		return method;
	}

	public ReconstructorPredicate getPredicate() {
		return predicate;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		MethodInsnNode methodins = (MethodInsnNode) ins;
		//ins is the INVOKESTATIC/INVOKEVIRTUAL instruction
		Object[] args = new Object[parameterTypes.length];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[args.length];
		try {
			if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins,
					args, derivedargs)) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newMethodArgumentsReconstructionException(e, ins, methodins.owner, methodins.name,
					methodins.desc);
		}
		return reconstructValueWithArgs(context, methodins, args, derivedargs);
	}

	AsmStackReconstructedValue reconstructValueWithArgs(ReconstructionContext context, MethodInsnNode methodins,
			Object[] args, AsmStackReconstructedValue[] derivedargs) throws ReconstructionException {
		AbstractInsnNode firstins = args.length == 0 ? methodins : derivedargs[0].getFirstIns();
		Object subject;
		AsmStackReconstructedValue subjectval;
		boolean staticfunc = methodins.getOpcode() == Opcodes.INVOKESTATIC;
		if (staticfunc) {
			subjectval = null;
			subject = null;
		} else {
			Class<?> declaringclass = method.getDeclaringClass();
			try {
				subjectval = context.getInliner().reconstructStackValue(context.withReceiverType(declaringclass),
						firstins.getPrevious());
				if (subjectval == null) {
					return null;
				}
				firstins = subjectval.getFirstIns();
				subject = subjectval.getValue();
			} catch (ReconstructionException e) {
				throw context.newInstanceAccessFailureReconstructionException(e, methodins, methodins.owner,
						methodins.name, methodins.desc);
			}

			if (subject == null) {
				throw context.newMethodInvocationFailureReconstructionException(
						new NullPointerException("Method call subject is null: " + method), methodins, methodins.owner,
						methodins.name, methodins.desc, subject, args);
			}
			if (!declaringclass.isInstance(subject)) {
				//the reconstructed subject is not an instance of the method declaring class
				//we can't call this method on it
				//this can happen if this method is called on other method instructions that the one described
				//by the method  field
				return null;
			}
		}

		if (!predicate.canReconstruct(subject, method, args)) {
			context.getInliner().logReconstructionNotAllowed(subject, method, args);
			return null;
		}

		Object resultobj;
		try {
			resultobj = method.invoke(subject, args);
		} catch (Exception e) {
			throw context.newMethodInvocationFailureReconstructionException(e, methodins, methodins.owner,
					methodins.name, methodins.desc, subject, args);
		}
		AsmStackInfo stackinfo;
		if (staticfunc) {
			stackinfo = AsmStackInfo.createStaticMethod(Type.getObjectType(methodins.owner), methodins.name,
					Type.getMethodType(methodins.desc), AsmStackReconstructedValue.toStackInfoArray(derivedargs));
		} else {
			stackinfo = AsmStackInfo.createMethod(Type.getObjectType(methodins.owner), methodins.name,
					Type.getMethodType(methodins.desc), subjectval.getStackInfo(),
					AsmStackReconstructedValue.toStackInfoArray(derivedargs));
		}
		return new AsmStackReconstructedValue(firstins, methodins.getNext(), stackinfo, resultobj);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[method=");
		builder.append(method);
		builder.append(", predicate=");
		builder.append(predicate);
		builder.append("]");
		return builder.toString();
	}
}