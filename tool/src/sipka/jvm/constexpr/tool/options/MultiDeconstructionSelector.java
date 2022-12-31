package sipka.jvm.constexpr.tool.options;

final class MultiDeconstructionSelector implements DeconstructionSelector {
	private final Iterable<? extends DeconstructionSelector> delegates;

	public MultiDeconstructionSelector(Iterable<? extends DeconstructionSelector> delegates) {
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

	public Iterable<? extends DeconstructionSelector> getDelegates() {
		return delegates;
	}
}