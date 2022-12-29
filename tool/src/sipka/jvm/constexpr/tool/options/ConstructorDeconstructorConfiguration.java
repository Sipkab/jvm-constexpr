package sipka.jvm.constexpr.tool.options;

import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

public final class ConstructorDeconstructorConfiguration extends ExecutableDeconstructorConfiguration {

	ConstructorDeconstructorConfiguration(Type executableOwner, Type[] executableParameterTypes,
			String[] getterMethodNames) {
		super(executableOwner, Utils.CONSTRUCTOR_METHOD_NAME, executableParameterTypes, getterMethodNames);
	}

}
