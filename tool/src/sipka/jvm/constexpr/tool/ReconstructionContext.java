package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.log.ArgumentLogContextInfo;
import sipka.jvm.constexpr.tool.log.BytecodeLocation;
import sipka.jvm.constexpr.tool.log.ClassNotFoundLogContextInfo;
import sipka.jvm.constexpr.tool.log.FieldAccessFailureContextInfo;
import sipka.jvm.constexpr.tool.log.FieldInliningLogContextInfo;
import sipka.jvm.constexpr.tool.log.FieldNotFoundLogContextInfo;
import sipka.jvm.constexpr.tool.log.InstanceAccessLogContextInfo;
import sipka.jvm.constexpr.tool.log.MethodArgumentsLogContextInfo;
import sipka.jvm.constexpr.tool.log.MethodInvocationFailureContextInfo;
import sipka.jvm.constexpr.tool.log.MethodNotFoundLogContextInfo;
import sipka.jvm.constexpr.tool.log.OpcodeArgumentLogContextInfo;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

class ReconstructionContext {
	private final ConstantExpressionInliner inliner;
	private final TransformedClass transformedClass;
	private final MethodNode methodNode;
	/**
	 * The type that receives the reconstructed value.
	 */
	private final Class<?> receiverType;
	private final ClassLoader classLoader;
	private final boolean forceReconstruct;

	private ReconstructionContext(ConstantExpressionInliner inliner, TransformedClass transformedClass,
			MethodNode methodNode, Class<?> receiverType, ClassLoader classLoader, boolean forceReconstruct) {
		this.inliner = inliner;
		this.transformedClass = transformedClass;
		this.methodNode = methodNode;
		this.receiverType = receiverType;
		this.classLoader = classLoader;
		this.forceReconstruct = forceReconstruct;
	}

	public static ReconstructionContext createConstantField(ConstantExpressionInliner inliner,
			TransformedClass transformedClass, Field f, MethodNode clinitmethod) {
		return new ReconstructionContext(inliner, transformedClass, clinitmethod, f.getType(),
				f.getDeclaringClass().getClassLoader(), true);
	}

	public static ReconstructionContext createForReceiverType(ConstantExpressionInliner inliner,
			TransformedClass transformedClass, Class<?> receiver, MethodNode method) {
		return new ReconstructionContext(inliner, transformedClass, method, receiver, null, false);
	}

	public ConstantExpressionInliner getInliner() {
		return inliner;
	}

	public TransformedClass getTransformedClass() {
		return transformedClass;
	}

	public MethodNode getMethodNode() {
		return methodNode;
	}

	public Class<?> getReceiverType() {
		return receiverType;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public boolean isForceReconstruct() {
		return forceReconstruct;
	}

	public ReconstructionContext withReceiverType(Class<?> type) {
		return new ReconstructionContext(inliner, transformedClass, methodNode, type, classLoader, forceReconstruct);
	}

	public ReconstructionContext forArgumentReconstruction() {
		if (receiverType == null) {
			return this;
		}
		return withReceiverType(null);
	}

	public ReconstructionException newArgumentIndexReconstructionException(ReconstructionException cause,
			AbstractInsnNode locationins, int argumentIndex) {
		return new ReconstructionException(cause,
				new ArgumentLogContextInfo(getBytecodeLocation(locationins), argumentIndex));
	}

	public ReconstructionException newOpcodeReconstructionException(ReconstructionException cause,
			AbstractInsnNode locationins, int argumentIndex, int opcode) {
		return new ReconstructionException(cause,
				new OpcodeArgumentLogContextInfo(getBytecodeLocation(locationins), argumentIndex, opcode));
	}

	public ReconstructionException newClassNotFoundReconstructionException(ClassNotFoundException e,
			AbstractInsnNode locationins, String classinternalname) {
		return new ReconstructionException(e,
				new ClassNotFoundLogContextInfo(getBytecodeLocation(locationins), classinternalname));
	}

	public ReconstructionException newMethodNotFoundReconstructionException(NoSuchMethodException e,
			AbstractInsnNode locationins, String classinternalname, String methodname, String methoddescriptor) {
		return new ReconstructionException(e, new MethodNotFoundLogContextInfo(getBytecodeLocation(locationins),
				classinternalname, methodname, methoddescriptor));
	}

	public ReconstructionException newMethodArgumentsReconstructionException(ReconstructionException e,
			AbstractInsnNode locationins, String classinternalname, String methodname, String methoddescriptor) {
		return new ReconstructionException(e, new MethodArgumentsLogContextInfo(getBytecodeLocation(locationins),
				classinternalname, methodname, methoddescriptor));
	}

	public ReconstructionException newMethodInvocationFailureReconstructionException(Exception e,
			AbstractInsnNode locationins, String classinternalname, String methodname, String methoddescriptor,
			Object instance, Object[] arguments) {
		return new ReconstructionException(e, new MethodInvocationFailureContextInfo(getBytecodeLocation(locationins),
				classinternalname, methodname, methoddescriptor, instance, arguments));
	}

	public ReconstructionException newFieldNotFoundReconstructionException(NoSuchFieldException e,
			AbstractInsnNode locationins, String classinternalname, String fieldname, String fielddescriptor) {
		return new ReconstructionException(e, new FieldNotFoundLogContextInfo(getBytecodeLocation(locationins),
				classinternalname, fieldname, fielddescriptor));
	}

	public ReconstructionException newFieldAccessFailureReconstructionException(Exception e,
			AbstractInsnNode locationins, String classinternalname, String fieldname, String fielddescriptor,
			Object instance) {
		return new ReconstructionException(e, new FieldAccessFailureContextInfo(getBytecodeLocation(locationins),
				classinternalname, fieldname, fielddescriptor, instance));
	}

	public ReconstructionException newFieldInliningReconstructionException(ReconstructionException e,
			AbstractInsnNode locationins, String classinternalname, String fieldname, String fielddescriptor) {
		return new ReconstructionException(e, new FieldInliningLogContextInfo(getBytecodeLocation(locationins),
				classinternalname, fieldname, fielddescriptor));
	}

	public ReconstructionException newInstanceAccessFailureReconstructionException(ReconstructionException e,
			AbstractInsnNode locationins, String classinternalname, String membername, String memberdescriptor) {
		return new ReconstructionException(e, new InstanceAccessLogContextInfo(getBytecodeLocation(locationins),
				classinternalname, membername, memberdescriptor));
	}

	private BytecodeLocation getBytecodeLocation(AbstractInsnNode locationins) {
		return inliner.getBytecodeLocation(transformedClass, methodNode, locationins);
	}

//	public ReconstructionException newReconstructionException(LogContextInfo contextinfo, Throwable rootcause,
//			AbstractInsnNode location) {
//		return new ReconstructionException(rootcause,
//				inliner.getBytecodeLocation(transformedClass, methodNode, location));
//	}
//
//	public ReconstructionException newReconstructionException(LogContextInfo contextinfo, ReconstructionException cause,
//			AbstractInsnNode location) {
//		return new ReconstructionException(cause, inliner.getBytecodeLocation(transformedClass, methodNode, location));
//	}

}
