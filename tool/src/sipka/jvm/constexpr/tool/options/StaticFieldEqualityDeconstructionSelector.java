package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

final class StaticFieldEqualityDeconstructionSelector implements DeconstructionSelector {
	private final Field[] fields;

	public StaticFieldEqualityDeconstructionSelector(Field[] fields) {
		this.fields = fields;
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
			Object value) {
		if (value == null) {
			//sanity check
			return null;
		}
		MemberReference optimizedmethod = deconstructioncontext.getOptimizedMethod();
		boolean inclinit = Utils.STATIC_INITIALIZER_METHOD_NAME.equals(optimizedmethod.getMemberName());
		for (Field field : fields) {
			if (inclinit) {
				if (Utils.hasSuperTypeInternalName(value.getClass(), optimizedmethod.getOwnerInternalName())) {
					//we're in the static initializer of the super type of the value type
					//this means that the value object cannot exist before this static initializer runs
					//so we cannot optimize it.
					continue;
				}
				if (Utils.hasSuperTypeInternalName(field.getDeclaringClass(), optimizedmethod.getOwnerInternalName())) {
					//ignore this one, as we're currently deconstructing in the static initializer of the
					//field declaring class (or one of its supertype)
					continue;
				}
			}
			try {
				if (value.equals(field.get(null))) {
					return DeconstructorConfiguration.createStaticField(field);
				}
			} catch (Exception e) {
				deconstructioncontext.logConfigClassMemberInaccessible(Type.getInternalName(field.getDeclaringClass()),
						field.getName(), Type.getType(field.getType()).getDescriptor(), e);
			}
		}
		return null;
	}

}
