package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

final class StaticFieldEqualityDeconstructionSelector implements DeconstructionSelector {
	private final DeconstructionSelector delegate;
	private final Field[] fields;

	public StaticFieldEqualityDeconstructionSelector(DeconstructionSelector delegate, Field[] fields) {
		this.delegate = delegate;
		this.fields = fields;
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(MemberReference optimizedmethod, Object value) {
		boolean inclinit = Utils.STATIC_INITIALIZER_METHOD_NAME.equals(optimizedmethod.getMemberName());
		for (Field field : fields) {
			if (inclinit
					&& Type.getInternalName(field.getDeclaringClass()).equals(optimizedmethod.getOwnerInternalName())) {
				//ignore this one, as we're currently deconstructing in the static initializer of the
				//field declaring class
				continue;
			}
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
		return delegate.chooseDeconstructorConfiguration(optimizedmethod, value);
	}

}
