package sipka.jvm.constexpr.tool;

import java.util.Arrays;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;

class TypeReferencedConstantReconstructor implements ConstantReconstructor {
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	protected final ConstantReconstructor delegate;
	/**
	 * Just an additional type reference for reflection access later in the inliner tool.
	 * <p>
	 * This type is not available through the {@link ConstantReconstructor}, but is relevant to make configuration
	 * easier, so its kept here internally.
	 * <p>
	 * Might be <code>null</code>.
	 */
	protected final Class<?>[] types;

	public TypeReferencedConstantReconstructor(ConstantReconstructor delegate, Class<?>... types) {
		this.delegate = delegate;
		this.types = types;
	}

	public TypeReferencedConstantReconstructor(ConstantReconstructor delegate) {
		this.delegate = delegate;
		this.types = EMPTY_CLASS_ARRAY;
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		return delegate.reconstructValue(context, ins);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[types=");
		builder.append(Arrays.toString(types));
		builder.append(", delegate=");
		builder.append(delegate);
		builder.append("]");
		return builder.toString();
	}

}