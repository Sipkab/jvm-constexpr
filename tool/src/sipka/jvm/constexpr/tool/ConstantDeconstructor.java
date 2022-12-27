package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

/**
 * Interface for serializing an inline value to ASM instructions so that they can be represented on the stack.
 */
interface ConstantDeconstructor {
	/**
	 * Writes ASM instructions after the <code>ins</code> parameter that deconstruct the argument as a constant
	 * representation.
	 * 
	 * @param context
	 *            The inliner context.
	 * @param transclass
	 *            The class being transformed,
	 * @param value
	 *            The value.
	 * @return The generated instruction list.
	 */
	public InsnList deconstructValue(ConstantExpressionInliner context, TransformedClass transclass, Object value);
}