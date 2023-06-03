package sipka.jvm.constexpr.tool;

import java.util.ArrayList;
import java.util.List;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.AbstractInsnNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodInsnNode;

/**
 * {@link ConstantReconstructor} that a suitable {@link MethodBasedConstantReconstructor}.
 */
final class DynamicInstanceMethodBasedConstantReconstructor implements ConstantReconstructor {
	private List<MethodBasedConstantReconstructor> reconstructors = new ArrayList<>();

	public DynamicInstanceMethodBasedConstantReconstructor(MethodBasedConstantReconstructor reconstructor1,
			MethodBasedConstantReconstructor reconstructor2) {
		this.reconstructors.add(reconstructor1);
		this.reconstructors.add(reconstructor2);
	}

	public void addReconstructor(MethodBasedConstantReconstructor reconstructor) {
		reconstructors.add(reconstructor);
	}

	@Override
	public AsmStackReconstructedValue reconstructValue(ReconstructionContext context, AbstractInsnNode ins)
			throws ReconstructionException {
		MethodInsnNode methodins = (MethodInsnNode) ins;
		//ins is the INVOKEVIRTUAL instruction

		//parameterTypes should be all the same for all reconstructors
		Class<?>[] parameterTypes = reconstructors.get(0).getParameterTypes();
		Object[] args = new Object[parameterTypes.length];
		AsmStackReconstructedValue[] derivedargs = new AsmStackReconstructedValue[args.length];
		try {
			if (!context.getInliner().reconstructArguments(context.forArgumentReconstruction(), parameterTypes, ins,
					args, derivedargs)) {
				return null;
			}
		} catch (ReconstructionException e) {
			throw context.newMethodArgumentsReconstructionException(e, ins, methodins.owner, methodins.name,
					methodins.desc);
		}
		for (MethodBasedConstantReconstructor reconstructor : reconstructors) {
			AsmStackReconstructedValue result = reconstructor.reconstructValueWithArgs(context, methodins, args,
					derivedargs);
			if (result == null) {
				continue;
			}
			return result;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(reconstructors);
		return builder.toString();
	}
}