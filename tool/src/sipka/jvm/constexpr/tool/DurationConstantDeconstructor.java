package sipka.jvm.constexpr.tool;

import java.time.Duration;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

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
			ConstantDeconstructor millisdeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Duration.class, "ofMillis", DeconstructionDataAccessor.createForMethod(Duration.class, "toMillis"));

			ConstantDeconstructor nanosdecosntructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Duration.class, "ofNanos", DeconstructionDataAccessor.createForMethod(Duration.class, "toNanos"));
			instance = new DurationConstantDeconstructor(nanosbaseddeconstructor, secondsbaseddeconstructor,
					millisdeconstructor, nanosdecosntructor);
		} catch (Exception e) {
			e.printStackTrace();
		}
		instance = new StaticFieldEqualityDelegateConstantDeconstructor(instance, Duration.class, "ZERO");
		INSTANCE = instance;
	}
	private final ConstantDeconstructor secondsAndNanosDeconstructor;
	private final ConstantDeconstructor secondsDeconstructor;
	private final ConstantDeconstructor millisDeconstructor;
	private final ConstantDeconstructor nanosDeconstructor;

	public DurationConstantDeconstructor(ConstantDeconstructor secondsAndNanosDeconstructor,
			ConstantDeconstructor secondsDeconstructor, ConstantDeconstructor millisDeconstructor,
			ConstantDeconstructor nanosDeconstructor) {
		this.secondsAndNanosDeconstructor = secondsAndNanosDeconstructor;
		this.secondsDeconstructor = secondsDeconstructor;
		this.millisDeconstructor = millisDeconstructor;
		this.nanosDeconstructor = nanosDeconstructor;
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		Duration dur = (Duration) value;
		int nano = dur.getNano();
		if (nano == 0) {
			//if the nanos part of the Duration is 0, then we can use the ofSeconds(long) method instead of ofSeconds(long,long)
			//for deconstruction
			return secondsDeconstructor.deconstructValue(context, transclass, methodnode, value);
		}
		try {
			//call to check arithmetic overflow
			dur.toNanos();
			return nanosDeconstructor.deconstructValue(context, transclass, methodnode, value);
		} catch (ArithmeticException e) {
			// overflow, doesn't work
		}
		if ((nano % 1_000_000) == 0) {
			//the nano and microseconds part is zero
			//so the resolution is not finer than milliseconds
			try {
				//call to check arithmetic overflow
				dur.toMillis();
				return millisDeconstructor.deconstructValue(context, transclass, methodnode, value);
			} catch (ArithmeticException e) {
				// overflow, doesn't work
			}
		}
		return secondsAndNanosDeconstructor.deconstructValue(context, transclass, methodnode, value);
	}
}