package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

final class StringBuilderConstantDeconstructor implements ConstantDeconstructor {
	private static final int DEFAULT_STRINGBUILDER_CAPACITY = new StringBuilder().capacity();
	private static final ConstantDeconstructor TOSTRING_DECONSTRUCTOR;
	private static final ConstantDeconstructor NOARG_DECONSTRUCTOR = ConstructorBasedDeconstructor
			.create(StringBuilder.class);
	private static final ConstantDeconstructor CAPACITY_DECONSTRUCTOR;
	static {
		try {
			TOSTRING_DECONSTRUCTOR = ConstructorBasedDeconstructor.create(StringBuilder.class,
					DeconstructionDataAccessor.createForMethod(StringBuilder.class, "toString"));
			CAPACITY_DECONSTRUCTOR = ConstructorBasedDeconstructor.create(StringBuilder.class,
					DeconstructionDataAccessor.createForMethod(StringBuilder.class, "capacity"));
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	public static final StringBuilderConstantDeconstructor INSTANCE = new StringBuilderConstantDeconstructor();

	private StringBuilderConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		StringBuilder sb = (StringBuilder) value;
		if (sb.length() == 0) {
			//to check if the stringbuilder was constructed with a capacity constructor
			int capacity = sb.capacity();
			if (capacity != DEFAULT_STRINGBUILDER_CAPACITY) {
				return CAPACITY_DECONSTRUCTOR.deconstructValue(context, transclass, methodnode, value);
			}
			return NOARG_DECONSTRUCTOR.deconstructValue(context, transclass, methodnode, value);
		}
		return TOSTRING_DECONSTRUCTOR.deconstructValue(context, transclass, methodnode, value);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}