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
		MemberReference optimizedmethod = deconstructioncontext.getOptimizedMethod();
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
			} catch (Exception e) {
				deconstructioncontext.logConfigClassMemberInaccessible(Type.getInternalName(field.getDeclaringClass()),
						field.getName(), Type.getType(field.getType()).getDescriptor(), e);
			}
		}
		return null;
	}

}
