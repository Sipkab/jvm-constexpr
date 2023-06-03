package sipka.jvm.constexpr.tool;

import java.time.LocalTime;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link LocalTime} class.
 */
final class LocalTimeConstantDeconstructor implements ConstantDeconstructor {
	private static final ConstantDeconstructor fieldEqualityDeconstructor = new StaticFieldEqualityConstantDeconstructor(
			Type.getType(LocalTime.class), LocalTime.class, "MIN", "MAX", "MIDNIGHT", "NOON");

	private static final ConstantDeconstructor hourMinDecon;
	private static final ConstantDeconstructor hourMinSecDecon;
	private static final ConstantDeconstructor allDecon;

	static {
		ConstantDeconstructor hourmindecon = null;
		ConstantDeconstructor hourminsecdecon = null;
		ConstantDeconstructor alldecon = null;
		try {
			hourmindecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(LocalTime.class, "of",
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			hourminsecdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(LocalTime.class, "of",
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getSecond"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			alldecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(LocalTime.class, "of",
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getHour"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getMinute"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getSecond"),
					DeconstructionDataAccessor.createForMethod(LocalTime.class, "getNano"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		hourMinDecon = hourmindecon;
		hourMinSecDecon = hourminsecdecon;
		allDecon = alldecon;
	}
	public static final ConstantDeconstructor INSTANCE = new LocalTimeConstantDeconstructor();

	public LocalTimeConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		DeconstructionResult fieldres = fieldEqualityDeconstructor.deconstructValue(context, transclass, methodnode,
				value);
		if (fieldres != null) {
			return fieldres;
		}
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