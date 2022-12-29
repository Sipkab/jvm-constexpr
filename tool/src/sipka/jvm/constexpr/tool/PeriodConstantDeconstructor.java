package sipka.jvm.constexpr.tool;

import java.time.Period;

/**
 * {@link ConstantDeconstructor} for the {@link Period} class.
 */
final class PeriodConstantDeconstructor implements ConstantDeconstructor {
	public static final ConstantDeconstructor INSTANCE;
	static {
		ConstantDeconstructor instance = null;
		try {
			ConstantDeconstructor yearsdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Period.class, "ofYears", DeconstructionDataAccessor.createForMethod(Period.class, "getYears"));
			ConstantDeconstructor monthsdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Period.class, "ofMonths", DeconstructionDataAccessor.createForMethod(Period.class, "getMonths"));
			ConstantDeconstructor daysdecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Period.class, "ofDays", DeconstructionDataAccessor.createForMethod(Period.class, "getDays"));
			ConstantDeconstructor alldecon = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Period.class, "of", DeconstructionDataAccessor.createForMethod(Period.class, "getYears"),
					DeconstructionDataAccessor.createForMethod(Period.class, "getMonths"),
					DeconstructionDataAccessor.createForMethod(Period.class, "getDays"));
			instance = new PeriodConstantDeconstructor(yearsdecon, monthsdecon, daysdecon, alldecon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = new StaticFieldEqualityDelegateConstantDeconstructor(instance, Period.class, "ZERO");
		INSTANCE = instance;
	}

	private final ConstantDeconstructor yearsDecon;
	private final ConstantDeconstructor monthsDecon;
	private final ConstantDeconstructor daysDecon;
	private final ConstantDeconstructor allDecon;

	public PeriodConstantDeconstructor(ConstantDeconstructor yearsdecon, ConstantDeconstructor monthsdecon,
			ConstantDeconstructor daysdecon, ConstantDeconstructor alldecon) {
		this.yearsDecon = yearsdecon;
		this.monthsDecon = monthsdecon;
		this.daysDecon = daysdecon;
		this.allDecon = alldecon;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		Period period = (Period) value;
		int y = period.getYears();
		int m = period.getMonths();
		int d = period.getDays();
		if (y == 0 && m == 0) {
			return daysDecon.deconstructValue(context, transclass, value);
		}
		if (y == 0 && d == 0) {
			return monthsDecon.deconstructValue(context, transclass, value);
		}
		if (m == 0 && d == 0) {
			return yearsDecon.deconstructValue(context, transclass, value);
		}
		return allDecon.deconstructValue(context, transclass, value);
	}
}