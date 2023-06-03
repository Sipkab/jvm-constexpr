package sipka.jvm.constexpr.tool;

import java.time.Period;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link Period} class.
 */
final class PeriodConstantDeconstructor implements ConstantDeconstructor {
	private static final ConstantDeconstructor fieldEqualityDeconstructor = new StaticFieldEqualityConstantDeconstructor(
			Type.getType(Period.class), Period.class, "ZERO");

	private static final ConstantDeconstructor yearsDecon;
	private static final ConstantDeconstructor monthsDecon;
	private static final ConstantDeconstructor daysDecon;
	private static final ConstantDeconstructor allDecon;
	static {
		ConstantDeconstructor yearsdecon = null;
		ConstantDeconstructor monthsdecon = null;
		ConstantDeconstructor daysdecon = null;
		ConstantDeconstructor alldecon = null;
		try {
			yearsdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Period.class, "ofYears",
					DeconstructionDataAccessor.createForMethod(Period.class, "getYears"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			monthsdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Period.class, "ofMonths",
					DeconstructionDataAccessor.createForMethod(Period.class, "getMonths"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			daysdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Period.class, "ofDays",
					DeconstructionDataAccessor.createForMethod(Period.class, "getDays"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			alldecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Period.class, "of",
					DeconstructionDataAccessor.createForMethod(Period.class, "getYears"),
					DeconstructionDataAccessor.createForMethod(Period.class, "getMonths"),
					DeconstructionDataAccessor.createForMethod(Period.class, "getDays"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		yearsDecon = yearsdecon;
		monthsDecon = monthsdecon;
		daysDecon = daysdecon;
		allDecon = alldecon;
	}
	public static final ConstantDeconstructor INSTANCE = new PeriodConstantDeconstructor();

	public PeriodConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		DeconstructionResult fieldres = fieldEqualityDeconstructor.deconstructValue(context, transclass, methodnode,
				value);
		if (fieldres != null) {
			return fieldres;
		}
		Period period = (Period) value;
		int y = period.getYears();
		int m = period.getMonths();
		int d = period.getDays();
		if (y == 0 && m == 0) {
			return daysDecon.deconstructValue(context, transclass, methodnode, value);
		}
		if (y == 0 && d == 0) {
			return monthsDecon.deconstructValue(context, transclass, methodnode, value);
		}
		if (m == 0 && d == 0) {
			return yearsDecon.deconstructValue(context, transclass, methodnode, value);
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