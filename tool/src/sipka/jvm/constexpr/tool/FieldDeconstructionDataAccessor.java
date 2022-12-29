package sipka.jvm.constexpr.tool;

import java.lang.reflect.Field;

public final class FieldDeconstructionDataAccessor implements DeconstructionDataAccessor {
	private final Field field;
	private final Class<?> receiverType;

	public FieldDeconstructionDataAccessor(Field f) {
		this(f, f.getType());
	}

	public FieldDeconstructionDataAccessor(Field field, Class<?> receiverType) {
		this.field = field;
		this.receiverType = receiverType;
	}

	@Override
	public DeconstructedData getData(Object value) throws Exception {
		return new DeconstructedData(field.get(value), receiverType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((receiverType == null) ? 0 : receiverType.hashCode());
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
		FieldDeconstructionDataAccessor other = (FieldDeconstructionDataAccessor) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (receiverType == null) {
			if (other.receiverType != null)
				return false;
		} else if (!receiverType.equals(other.receiverType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[field=");
		builder.append(field);
		builder.append(", receiverType=");
		builder.append(receiverType);
		builder.append("]");
		return builder.toString();
	}
}