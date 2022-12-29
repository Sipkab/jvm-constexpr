package sipka.jvm.constexpr.tool;

import java.time.Duration;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;

/**
 * {@link ConstantDeconstructor} for the {@link Duration} class.
 */
final class DurationConstantDeconstructor implements ConstantDeconstructor {
	public static final ConstantDeconstructor INSTANCE;
	static {
		ConstantDeconstructor instance = null;
		try {
			ConstantDeconstructor nanosbaseddeconstructor = StaticMethodBasedDeconstructor
					.createStaticFactoryDeconstructor(Duration.class, "ofSeconds",
							DeconstructionDataAccessor.createForMethod(Duration.class, "getSeconds"),
							DeconstructionDataAccessor.createForMethodWithReceiver(Duration.class, "getNano",
									long.class));
			ConstantDeconstructor secondsbaseddeconstructor = StaticMethodBasedDeconstructor
					.createStaticFactoryDeconstructor(Duration.class, "ofSeconds",
							DeconstructionDataAccessor.createForMethod(Duration.class, "getSeconds"));
			instance = new DurationConstantDeconstructor(nanosbaseddeconstructor, secondsbaseddeconstructor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = new StaticFieldEqualityDelegateConstantDeconstructor(instance, Duration.class, "ZERO");
		INSTANCE = instance;
	}
	private final ConstantDeconstructor secondsAndNanosDeconstructor;
	private final ConstantDeconstructor secondsDeconstructor;

	public DurationConstantDeconstructor(ConstantDeconstructor nanosbaseddeconstructor,
			ConstantDeconstructor secondsbaseddeconstructor) {
		this.secondsAndNanosDeconstructor = nanosbaseddeconstructor;
		this.secondsDeconstructor = secondsbaseddeconstructor;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			Object value) {
		//if the nanos part of the Duration is 0, then we can use the ofSeconds(long) method instead of ofSeconds(long,long)
		//for deconstruction
		Duration dur = (Duration) value;
		if (dur.getNano() == 0) {
			return secondsDeconstructor.deconstructValue(context, transclass, value);
		}
		return secondsAndNanosDeconstructor.deconstructValue(context, transclass, value);
	}
}