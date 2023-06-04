package sipka.jvm.constexpr.tool;

import java.time.LocalTime;

import sipka.jvm.constexpr.tool.options.DeconstructionContext;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;

final class LocalTimeDeconstructionSelector implements DeconstructionSelector {
	public LocalTimeDeconstructionSelector() {
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
			Object value) {
		DeconstructorConfiguration fieldeqres = Utils.createCheckStaticFieldEqualityDeconstructorConfiguration(
				deconstructioncontext, value, LocalTime.class, "NOON", "MIDNIGHT", "MIN", "MAX");
		if (fieldeqres != null) {
			return fieldeqres;
		}

		LocalTime lt = (LocalTime) value;
		if (lt.getNano() == 0) {
			if (lt.getSecond() == 0) {
				try {
					return DeconstructorConfiguration.createStaticMethod(
							Utils.getMethodReportInaccessible(deconstructioncontext, LocalTime.class, LocalTime.class,
									"of", int.class, int.class),
							DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
									deconstructioncontext, LocalTime.class, int.class, "getHour")),
							DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
									deconstructioncontext, LocalTime.class, int.class, "getMinute")));
				} catch (NoSuchMethodException e) {
					// error is already logged in the utils calls
				}
			}

			try {
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, LocalTime.class, LocalTime.class, "of",
								int.class, int.class, int.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, LocalTime.class, int.class, "getHour")),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, LocalTime.class, int.class, "getMinute")),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, LocalTime.class, int.class, "getSecond")));
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}

		try {
			return DeconstructorConfiguration.createStaticMethod(
					Utils.getMethodReportInaccessible(deconstructioncontext, LocalTime.class, LocalTime.class, "of",
							int.class, int.class, int.class, int.class),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							LocalTime.class, int.class, "getHour")),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							LocalTime.class, int.class, "getMinute")),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							LocalTime.class, int.class, "getSecond")),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							LocalTime.class, int.class, "getNano")));
		} catch (NoSuchMethodException e) {
			// error is already logged in the utils calls
		}
		return null;
	}

}