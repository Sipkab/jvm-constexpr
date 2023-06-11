package sipka.jvm.constexpr.tool;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sipka.jvm.constexpr.tool.options.ReconstructorPredicate;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * {@link ConstantReconstructor} that calls a static or instance method to reconstruct a value.
 */
final class MethodBasedConstantReconstructor implements ConstantReconstructor {
	private final String methodOwner;
	private final String methodName;
	private final String methodDescriptor;
	private final boolean staticMethod;
	private final ReconstructorPredicate predicate;

	private final transient Class<?>[] parameterTypes;

	public MethodBasedConstantReconstructor(Method m, ReconstructorPredicate predicate) {
		this.methodOwner = Type.getInternalName(m.getDeclaringClass());
		this.methodName = m.getName();
		this.methodDescriptor = Type.getMethodDescriptor(m);
		this.staticMethod = Modifier.isStatic(m.getModifiers());
		this.predicate = predicate;
		this.parameterTypes = m.getParameterTypes();
	}

	public MethodBasedConstantReconstructor(String methodOwner, String methodName, String methodDescriptor,
			boolean staticMethod, ReconstructorPredicate predicate) {
		this.methodOwner = methodOwner;
		this.methodName = methodName;
		this.methodDescriptor = methodDescriptor;
		this.staticMethod = staticMethod;
		this.predicate = predicate;
		this.parameterTypes = null; // TODO fill this?
	}

	public boolean isStaticMethod() {
		return staticMethod;
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
		Type[] argasmtypes = Type.getArgumentTypes(methodDescriptor);
		Object[] args = new Object[argasmtypes.length];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[args.length];
		try {
			if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), argasmtypes,
					parameterTypes, ins, args, derivedargs)) {
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
		Class<?> declaringclass;
		try {
			declaringclass = context.getInliner().findClass(Type.getObjectType(methodOwner));
		} catch (ClassNotFoundException e) {
			throw context.newClassNotFoundReconstructionException(e, methodins, methodOwner);
		}
		Method method;
		if (staticfunc) {
			subjectval = null;
			subject = null;
			try {
				method = Utils.getDeclaredMethodWithDescriptor(declaringclass, methodName, methodDescriptor);
			} catch (NoSuchMethodException e) {
				throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner, methodins.name,
						methodins.desc);
			}
		} else {
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
						new NullPointerException("Method call subject is null: " + Utils.memberDescriptorToPrettyString(
								Type.getMethodType(methodDescriptor), Type.getObjectType(methodOwner), methodName)),
						methodins, methodins.owner, methodins.name, methodins.desc, subject, args);
			}
			if (!Utils.hasSuperTypeInternalName(subject.getClass(), methodOwner)) {
				//the reconstructed subject is not an instance of the method declaring class
				//we can't call this method on it
				//this can happen if this method is called on other method instructions that the one described
				//by the method  field
				return null;
			}
			try {
				method = Utils.getMethodForMethodDescriptor(subject.getClass(), methodOwner, methodDescriptor,
						methodName);
			} catch (NoSuchMethodException e) {
				throw context.newMethodNotFoundReconstructionException(e, methodins, methodins.owner, methodins.name,
						methodins.desc);
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
		builder.append("[methodOwner=");
		builder.append(methodOwner);
		builder.append(", methodName=");
		builder.append(methodName);
		builder.append(", methodDescriptor=");
		builder.append(methodDescriptor);
		builder.append(", staticMethod=");
		builder.append(staticMethod);
		builder.append(", predicate=");
		builder.append(predicate);
		builder.append("]");
		return builder.toString();
	}
}