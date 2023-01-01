package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * {@link ConstantReconstructor} that calls {@link Object#toString()} on instances for which this method was not
 * specified explicitly.
 */
final class ToStringConstantReconstructor implements ConstantReconstructor {
	public static final ToStringConstantReconstructor INSTANCE = new ToStringConstantReconstructor();

	public ToStringConstantReconstructor() {
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		//ins is the INVOKEVIRTUAL instruction
		final String methoddescriptor = "()Ljava/lang/String;";
		final Type methodownertype = Type.getObjectType(((MethodInsnNode) ins).owner);
		final String methodname = "toString";

		AsmStackReconstructedValue subjectval;
		try {
			subjectval = context.getInliner().reconstructStackValue(context.withReceiverType(Object.class),
					ins.getPrevious());
			if (subjectval == null) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newInstanceAccessFailureReconstructionException(e, ins, methodownertype.getInternalName(),
					methodname, methoddescriptor);
		}
		Object subject = subjectval.getValue();
		if (subject == null) {
			throw context.newMethodInvocationFailureReconstructionException(
					new NullPointerException("Method call subject is null: Object.toString()"), ins,
					methodownertype.getInternalName(), methodname, methoddescriptor, subject, new Object[0]);
		}
		Class<?> subjectclass = subject.getClass();

		String resultobj;
		try {
			if ((subjectclass.isArray() || subjectclass == Object.class) && !context.isForceReconstruct()) {
				//Object and Array[] string representations contain the identity hashcode
				//only allow if force reconstruct
				return null;
			}
			resultobj = subject.toString();
		} catch (Exception e) {
			throw context.newMethodInvocationFailureReconstructionException(e, ins, methodownertype.getInternalName(),
					methodname, methoddescriptor, subject, new Object[0]);
		}

		if ((subjectclass.getName() + "@" + Integer.toHexString(System.identityHashCode(subject))).equals(resultobj)) {
			//the string representation seems to be the same as the default Object.toString() representation
			//probably because it wasnt overridden by the subclass

			if (!context.getInliner().isConstantType(Type.getInternalName(subjectclass))) {
				//if not a constant type, then don't accept this string representation
				if (!context.isForceReconstruct()) {
					return null;
				}
				//allowed if force reconstruct
			}
			context.getInliner().logIndeterministicToString(Type.getInternalName(subjectclass));
		}

		AsmStackInfo stackinfo = AsmStackInfo.createMethod(methodownertype, methodname,
				Type.getMethodType(methoddescriptor), subjectval.getStackInfo(), AsmStackInfo.EMPTY_ASMSTACKINFO_ARRAY);
		return new AsmStackReconstructedValue(subjectval.getFirstIns(), ins.getNext(), stackinfo, resultobj);
	}

}