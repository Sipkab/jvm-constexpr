package sipka.jvm.constexpr.tool;

import java.time.Duration;

import sipka.jvm.constexpr.tool.options.DeconstructionContext;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;

final class DurationDeconstructionSelector implements DeconstructionSelector {
	public DurationDeconstructionSelector() {
	}

	@Override
	public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
			Object value) {
		DeconstructorConfiguration fieldeqres = Utils.createCheckStaticFieldEqualityDeconstructorConfiguration(
				deconstructioncontext, value, Duration.class, "ZERO");
		if (fieldeqres != null) {
			return fieldeqres;
		}
		Duration dur = (Duration) value;
		int nano = dur.getNano();
		if (nano == 0) {
			//if the nanos part of the Duration is 0, then we can use the ofSeconds(long) method instead of ofSeconds(long,long)
			//for deconstruction
			try {
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, Duration.class, Duration.class,
								"ofSeconds", long.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, Duration.class, long.class, "getSeconds")));
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}
		try {
			//call to check arithmetic overflow
			dur.toNanos();
			return DeconstructorConfiguration.createStaticMethod(
					Utils.getMethodReportInaccessible(deconstructioncontext, Duration.class, Duration.class, "ofNanos",
							long.class),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							Duration.class, long.class, "toNanos")));
		} catch (ArithmeticException e) {
			// overflow, doesn't work
		} catch (NoSuchMethodException e) {
			// error is already logged in the utils calls
		}
		if ((nano % 1_000_000) == 0) {
			//the nano and microseconds part is zero
			//so the resolution is not finer than milliseconds
			try {
				//call to check arithmetic overflow
				dur.toMillis();
				return DeconstructorConfiguration.createStaticMethod(
						Utils.getMethodReportInaccessible(deconstructioncontext, Duration.class, Duration.class,
								"ofMillis", long.class),
						DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(
								deconstructioncontext, Duration.class, long.class, "toMillis")));
			} catch (ArithmeticException e) {
				// overflow, doesn't work
			} catch (NoSuchMethodException e) {
				// error is already logged in the utils calls
			}
		}
		try {
			return DeconstructorConfiguration.createStaticMethod(
					Utils.getMethodReportInaccessible(deconstructioncontext, Duration.class, Duration.class,
							"ofSeconds", long.class),
					DeconstructionDataAccessor.createForMethod(Utils.getMethodReportInaccessible(deconstructioncontext,
							Duration.class, long.class, "getSeconds")),
					DeconstructionDataAccessor.createForMethodWithReceiver(Utils.getMethodReportInaccessible(
							deconstructioncontext, Duration.class, long.class, "getNano"), long.class));
		} catch (NoSuchMethodException e) {
			// error is already logged in the utils calls
		}
		return null;
	}

}