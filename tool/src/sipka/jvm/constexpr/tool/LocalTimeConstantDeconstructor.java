package sipka.jvm.constexpr.tool;

import java.time.LocalTime;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link LocalTime} class.
 */
final class LocalTimeConstantDeconstructor implements ConstantDeconstructor {
	public static final ConstantDeconstructor INSTANCE;
	static {
		ConstantDeconstructor instance = null;
		try {
			ConstantDeconstructor hourmindecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					LocalTime.class, "of", DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"));
			ConstantDeconstructor hourminsecdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					LocalTime.class, "of", DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getSecond"));
			ConstantDeconstructor alldecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					LocalTime.class, "of", DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getSecond"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getNano"));
			instance = new LocalTimeConstantDeconstructor(hourmindecon, hourminsecdecon, alldecon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = new StaticFieldEqualityDelegateConstantDeconstructor(instance, Type.getType(LocalTime.class),
				LocalTime.class, "MIN", "MAX", "MIDNIGHT", "NOON");
		INSTANCE = instance;
	}

	private final ConstantDeconstructor hourMinDecon;
	private final ConstantDeconstructor hourMinSecDecon;
	private final ConstantDeconstructor allDecon;

	private LocalTimeConstantDeconstructor(ConstantDeconstructor hourmindecon, ConstantDeconstructor hourminsecdecon,
			ConstantDeconstructor alldecon) {
		this.hourMinDecon = hourmindecon;
		this.hourMinSecDecon = hourminsecdecon;
		this.allDecon = alldecon;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		LocalTime lt = (LocalTime) value;
		if (lt.getNano() == 0) {
			if (lt.getSecond() == 0) {
				return hourMinDecon.deconstructValue(context, transclass, methodnode, value);
			}
			return hourMinSecDecon.deconstructValue(context, transclass, methodnode, value);
		}
		return allDecon.deconstructValue(context, transclass, methodnode, value);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}