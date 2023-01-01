package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class TypeReferencedConstantReconstructor implements ConstantReconstructor {
	protected final ConstantReconstructor delegate;
	/**
	 * Just an additional type reference for reflection access later in the inliner tool.
	 * <p>
	 * This type is not available through the {@link ConstantReconstructor}, but is relevant to make configuration
	 * easier, so its kept here internally.
	 * <p>
	 * Might be <code>null</code>.
	 */
	protected final Class<?> type;

	public TypeReferencedConstantReconstructor(ConstantReconstructor delegate, Class<?> type) {
		this.delegate = delegate;
		this.type = type;
	}

	public TypeReferencedConstantReconstructor(ConstantReconstructor delegate) {
		this.delegate = delegate;
		this.type = null;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		return delegate.reconstructValue(context, ins);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[type=");
		builder.append(type);
		builder.append(", delegate=");
		builder.append(delegate);
		builder.append("]");
		return builder.toString();
	}

}