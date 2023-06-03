package sipka.jvm.constexpr.tool.options;

import java.util.List;

final class MultiDeconstructionSelector implements DeconstructionSelector {
	private final List<? extends DeconstructionSelector> delegates;

	public MultiDeconstructionSelector(List<? extends DeconstructionSelector> delegates) {
		this.delegates = delegates;
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(MemberReference optimizedmethod, Object value) {
		for (DeconstructionSelector selector : delegates) {
			DeconstructorConfiguration config = selector.chooseDeconstructorConfiguration(optimizedmethod, value);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	public List<? extends DeconstructionSelector> getDelegates() {
		return delegates;
	}
}