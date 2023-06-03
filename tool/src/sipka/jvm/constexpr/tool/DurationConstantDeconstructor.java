package sipka.jvm.constexpr.tool;

import java.time.Duration;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.MethodNode;

/**
 * {@link ConstantDeconstructor} for the {@link Duration} class.
 */
final class DurationConstantDeconstructor implements ConstantDeconstructor {
	private static final ConstantDeconstructor fieldEqualityDeconstructor = new StaticFieldEqualityConstantDeconstructor(
			Type.getType(Duration.class), Duration.class, "ZERO");

	private static final ConstantDeconstructor secondsAndNanosDeconstructor;
	private static final ConstantDeconstructor secondsDeconstructor;
	private static final ConstantDeconstructor millisDeconstructor;
	private static final ConstantDeconstructor nanosDeconstructor;
	static {
		ConstantDeconstructor secondsandnanosdeconstructor = null;
		ConstantDeconstructor secondsdeconstructor = null;
		ConstantDeconstructor millisdeconstructor = null;
		ConstantDeconstructor nanosdeconstructor = null;

		try {
			secondsandnanosdeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Duration.class, "ofSeconds",
					DeconstructionDataAccessor.createForMethod(Duration.class, "getSeconds"),
					DeconstructionDataAccessor.createForMethodWithReceiver(Duration.class, "getNano", long.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			secondsdeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Duration.class,
					"ofSeconds", DeconstructionDataAccessor.createForMethod(Duration.class, "getSeconds"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			millisdeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Duration.class,
					"ofMillis", DeconstructionDataAccessor.createForMethod(Duration.class, "toMillis"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			nanosdeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(Duration.class,
					"ofNanos", DeconstructionDataAccessor.createForMethod(Duration.class, "toNanos"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		secondsAndNanosDeconstructor = secondsandnanosdeconstructor;
		secondsDeconstructor = secondsdeconstructor;
		millisDeconstructor = millisdeconstructor;
		nanosDeconstructor = nanosdeconstructor;
	}

	public static final ConstantDeconstructor INSTANCE = new DurationConstantDeconstructor();

	public DurationConstantDeconstructor() {
	}

	@Override
	public DeconstructionResult deconstructValue(ConstantExpressionInliner context, TransformedClass transclass,
			MethodNode methodnode, Object value) {
		DeconstructionResult fieldres = fieldEqualityDeconstructor.deconstructValue(context, transclass, methodnode,
				value);
		if (fieldres != null) {
			return fieldres;
		}
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[]");
		return builder.toString();
	}
}