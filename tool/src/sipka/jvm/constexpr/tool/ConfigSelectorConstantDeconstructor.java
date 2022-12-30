package sipka.jvm.constexpr.tool;

import sipka.jvm.constexpr.tool.options.ConstructorDeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.FieldDeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.StaticMethodDeconstructorConfiguration;

final class ConfigSelectorConstantDeconstructor implements ConstantDeconstructor {
	private final DeconstructionSelector selector;

	public ConfigSelectorConstantDeconstructor(DeconstructionSelector selector) {
		this.selector = selector;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		DeconstructorConfiguration config = selector.chooseDeconstructorConfiguration(value);
		if (config == null) {
			return null;
		}
		ConstantDeconstructor deconstructor;
		if (config instanceof ConstructorDeconstructorConfiguration) {
			ConstructorDeconstructorConfiguration conconfig = (ConstructorDeconstructorConfiguration) config;
			deconstructor = ConstructorBasedDeconstructor.create(config.getMemberOwner(),
					conconfig.getExecutableParameterDataAccessors());
		} else if (config instanceof StaticMethodDeconstructorConfiguration) {
			StaticMethodDeconstructorConfiguration methodconfig = (StaticMethodDeconstructorConfiguration) config;

			deconstructor = StaticMethodBasedDeconstructor.createStaticMethodDeconstructor(
					methodconfig.getExecutableReturnType(), config.getMemberOwner(), config.getMemberName(),
					methodconfig.getExecutableParameterDataAccessors());
		} else if (config instanceof FieldDeconstructorConfiguration) {
			FieldDeconstructorConfiguration fieldconfig = (FieldDeconstructorConfiguration) config;
			deconstructor = FieldConstantDeconstructor.createStaticFieldDeconstructor(
					fieldconfig.getMemberOwner().getInternalName(), config.getMemberName(),
					fieldconfig.getFieldDescriptor().getDescriptor());
		} else {
			throw new IllegalArgumentException("Unrecognized " + DeconstructorConfiguration.class.getSimpleName()
					+ " subclass: " + config.getClass());
		}
		return deconstructor.deconstructValue(context, transclass, value);
	}
}