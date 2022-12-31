package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

final class StringBuilderConstantDeconstructor implements ConstantDeconstructor {
	private static final ConstantDeconstructor TOSTRING_DECONSTRUCTOR;
	private static final ConstantDeconstructor NOARG_DECONSTRUCTOR = ConstructorBasedDeconstructor
			.create(StringBuilder.class);
	static {
		try {
			TOSTRING_DECONSTRUCTOR = ConstructorBasedDeconstructor.create(StringBuilder.class,
					DeconstructionDataAccessor.createForMethod(StringBuilder.class, "toString"));
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	public static final StringBuilderConstantDeconstructor INSTANCE = new StringBuilderConstantDeconstructor();

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		StringBuilder sb = (StringBuilder) value;
		if (sb.length() == 0) {
			return NOARG_DECONSTRUCTOR.deconstructValue(context, transclass, methodnode, value);
		}
		return TOSTRING_DECONSTRUCTOR.deconstructValue(context, transclass, methodnode, value);
	}
}