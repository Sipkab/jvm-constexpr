package sipka.jvm.constexpr.tool;

import java.time.Duration;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.InsnList;

/**
 * {@link ConstantDeconstructor} for the {@link Duration} class.
 */
final class DurationConstantDeconstructor implements ConstantDeconstructor {
	public static final ConstantDeconstructor INSTANCE;
	static {
		ConstantDeconstructor instance = null;
		try {
			ConstantDeconstructor nanosbaseddeconstructor = StaticMethodBasedDeconstructor.createStaticFactoryDeconstructor(
					Duration.class, "ofSeconds", new Type[] { null, Type.getType(long.class), }, "getSeconds",
					"getNano");
			ConstantDeconstructor secondsbaseddeconstructor = StaticMethodBasedDeconstructor
					.createStaticFactoryDeconstructor(Duration.class, "ofSeconds", "getSeconds");
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
	public InsnList deconstructValue(ConstantExpressionInliner context, TransformedClass transclass, Object value) {
		//if the nanos part of the Duration is 0, then we can use the ofSeconds(long) method instead of ofSeconds(long,long)
		//for deconstruction
		Duration dur = (Duration) value;
		if (dur.getNano() == 0) {
			return secondsDeconstructor.deconstructValue(context, transclass, value);
		}
		return secondsAndNanosDeconstructor.deconstructValue(context, transclass, value);
	}
}