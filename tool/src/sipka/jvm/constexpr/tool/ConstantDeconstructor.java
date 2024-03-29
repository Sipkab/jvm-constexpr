package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

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
	 * @param methodnode
	 *            The method in which the deconstruction is happening.
	 * @param value
	 *            The value.
	 * @return The generated instruction list.
	 */
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value);
}