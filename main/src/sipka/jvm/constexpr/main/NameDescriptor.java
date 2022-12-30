package sipka.jvm.constexpr.main;

final class NameDescriptor implements Comparable<NameDescriptor> {
	protected final String name;
	protected final String descriptor;

	public NameDescriptor(String name, String descriptor) {
		this.name = name;
		this.descriptor = descriptor;
	}

	@Override
	public int compareTo(NameDescriptor o) {
		int cmp = this.name.compareTo(o.name);
		if (cmp != 0) {
			return cmp;
		}
		return this.descriptor.compareTo(o.descriptor);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NameDescriptor other = (NameDescriptor) obj;
		if (descriptor == null) {
			if (other.descriptor != null)
				return false;
		} else if (!descriptor.equals(other.descriptor))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[name=");
		builder.append(name);
		builder.append(", ");
		builder.append("descriptor=");
		builder.append(descriptor);
		builder.append("]");
		return builder.toString();
	}

}