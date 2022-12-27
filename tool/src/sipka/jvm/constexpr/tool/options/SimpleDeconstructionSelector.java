package sipka.jvm.constexpr.tool.options;

import java.util.Objects;

final class SimpleDeconstructionSelector implements DeconstructionSelector {
	private final DeconstructorConfiguration config;

	public SimpleDeconstructionSelector(DeconstructorConfiguration config) {
		Objects.requireNonNull(config, "config");
		this.config = config;
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(Object val) {
		return config;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleDeconstructionSelector other = (SimpleDeconstructionSelector) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[config=");
		builder.append(config);
		builder.append("]");
		return builder.toString();
	}
}