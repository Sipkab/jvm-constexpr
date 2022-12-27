package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

class ReconstructionContext {
	private final ConstantExpressionInliner inliner;
	private final TransformedClass transformedClass;
	/**
	 * The type that receives the reconstructed value.
	 */
	private final Class<?> receiverType;
	private final ClassLoader classLoader;
	private final boolean forceReconstruct;

	public ReconstructionContext(ConstantExpressionInliner inliner, TransformedClass transformedClass,
			Class<?> receiverType, ClassLoader classLoader, boolean forceReconstruct) {
		this.inliner = inliner;
		this.transformedClass = transformedClass;
		this.receiverType = receiverType;
		this.classLoader = classLoader;
		this.forceReconstruct = forceReconstruct;
	}

	public static ReconstructionContext createConstantField(ConstantExpressionInliner inliner,
			TransformedClass transformedClass, Field f) {
		return new ReconstructionContext(inliner, transformedClass, f.getType(), f.getDeclaringClass().getClassLoader(),
				true);
	}

	public static ReconstructionContext createForReceiverType(ConstantExpressionInliner inliner,
			TransformedClass transformedClass, Class<?> receiver) {
		return new ReconstructionContext(inliner, transformedClass, receiver, null, false);
	}

	public ConstantExpressionInliner getInliner() {
		return inliner;
	}

	public TransformedClass getTransformedClass() {
		return transformedClass;
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
		return new ReconstructionContext(inliner, transformedClass, type, classLoader, forceReconstruct);
	}

	public ReconstructionContext forArgumentReconstruction() {
		if (receiverType == null) {
			return this;
		}
		return withReceiverType(null);
	}

}
