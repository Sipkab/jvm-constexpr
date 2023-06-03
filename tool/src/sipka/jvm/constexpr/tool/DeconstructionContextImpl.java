package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.options.DeconstructionContext;
import sipka.jvm.constexpr.tool.options.MemberReference;

final class DeconstructionContextImpl implements DeconstructionContext {

	private final ConstantExpressionInliner inliner;
	private final MemberReference optimizedMethod;

	public DeconstructionContextImpl(ConstantExpressionInliner inliner, MemberReference optimizedMethod) {
		this.inliner = inliner;
		this.optimizedMethod = optimizedMethod;
	}

	@Override
	public MemberReference getOptimizedMethod() {
		return optimizedMethod;
	}

	@Override
	public void logConfigClassMemberInaccessible(String classInternalName, String memberName, String memberDescriptor,
			Throwable exception) {
		inliner.logConfigClassMemberInaccessible(classInternalName, memberName, memberDescriptor, exception);
	}

}
