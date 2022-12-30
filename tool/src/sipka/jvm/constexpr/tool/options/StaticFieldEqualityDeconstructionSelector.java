package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;

final class StaticFieldEqualityDeconstructionSelector implements DeconstructionSelector {
	private final DeconstructionSelector delegate;
	private final Field[] fields;

	public StaticFieldEqualityDeconstructionSelector(DeconstructionSelector delegate, Field[] fields) {
		this.delegate = delegate;
		this.fields = fields;
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(Object value) {
		for (Field field : fields) {
			try {
				if (value.equals(field.get(null))) {
					return DeconstructorConfiguration.createStaticField(field);
				}
			} catch (IllegalAccessException e) {
				// TODO logging?
				e.printStackTrace();
			}
		}
		if (delegate == null) {
			return null;
		}
		return delegate.chooseDeconstructorConfiguration(value);
	}

}
