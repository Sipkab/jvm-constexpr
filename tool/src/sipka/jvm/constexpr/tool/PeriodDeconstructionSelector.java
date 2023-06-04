package sipka.jvm.constexpr.tool;

import java.time.Period;

import sipka.jvm.constexpr.tool.options.DeconstructionContext;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;

final class PeriodDeconstructionSelector implements DeconstructionSelector {
	public PeriodDeconstructionSelector() {
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
			Object value) {
		DeconstructorConfiguration fieldeqres = Utils.createCheckStaticFieldEqualityDeconstructorConfiguration(
				deconstructioncontext, value, Period.class, "ZERO");
		if (fieldeqres != null) {
			return fieldeqres;
		}

		Period period = (Period) value;
		int y = period.getYears();
		int m = period.getMonths();
		int d = period.getDays();
		if (y == 0 && m == 0) {
			try {
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, Period.class, Period.class, "ofDays",
								int.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, Period.class, int.class, "getDays")));
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}
		if (y == 0 && d == 0) {
			try {
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, Period.class, Period.class, "ofMonths",
								int.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, Period.class, int.class, "getMonths")));
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}
		if (m == 0 && d == 0) {
			try {
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, Period.class, Period.class, "ofYears",
								int.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, Period.class, int.class, "getYears")));
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}

		try {
			return DeconstructorConfiguration.createStaticMethod(
					Utils.getMethodReportInaccessible(deconstructioncontext, Period.class, Period.class, "of",
							int.class, int.class, int.class),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							Period.class, int.class, "getYears")),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							Period.class, int.class, "getMonths")),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							Period.class, int.class, "getDays")));
		} catch (NoSuchMethodException e) {
			// error is already logged in the utils calls
		}
		return null;
	}

}