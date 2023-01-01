package testing.sipka.jvm.constexpr;

import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests known {@link String} functions.
 */
@SakerTest
public class MathFunctionsInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(Constants.class);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
		assertNull(TestUtils.getClInitMethod(classnode), "clinit method");
	}

	public static class Constants {
		//constants are generated, with somewhat random numbers
		public static final double sin_double_double_1 = Math.sin(0.44931370829494865d);
		public static final double cos_double_double_1 = Math.cos(0.8286744164420513d);
		public static final double tan_double_double_1 = Math.tan(0.8251845127663897d);
		public static final double asin_double_double_1 = Math.asin(0.8541072924384452d);
		public static final double acos_double_double_1 = Math.acos(0.6852050691347006d);
		public static final double atan_double_double_1 = Math.atan(1.1982059211436527d);
		public static final double toRadians_double_double_1 = Math.toRadians(0.7939984844620597d);
		public static final double toDegrees_double_double_1 = Math.toDegrees(1.3050182328771345d);
		public static final double exp_double_double_1 = Math.exp(0.619961069383714d);
		public static final double log_double_double_1 = Math.log(0.863281440270178d);
		public static final double log10_double_double_1 = Math.log10(0.6215996545185388d);
		public static final double sqrt_double_double_1 = Math.sqrt(1.2367256516596616d);
		public static final double cbrt_double_double_1 = Math.cbrt(0.7543659884857293d);
		public static final double IEEEremainder_double_double_double_1 = Math.IEEEremainder(0.5466360010216715d,
				1.3313658299357065d);
		public static final double ceil_double_double_1 = Math.ceil(0.6722483867398167d);
		public static final double floor_double_double_1 = Math.floor(1.0931980411038142d);
		public static final double rint_double_double_1 = Math.rint(0.6122282632328903d);
		public static final double atan2_double_double_double_1 = Math.atan2(1.1862352653015504d, 0.9880988912497319d);
		public static final double pow_double_double_double_1 = Math.pow(0.6099243013100148d, 0.43205177357276436d);
		public static final int round_float_int_1 = Math.round(0.69362724f);
		public static final long round_double_long_1 = Math.round(0.9623275009615879d);
		public static final int addExact_int_int_int_1 = Math.addExact(404, 949);
		public static final long addExact_long_long_long_1 = Math.addExact(142L, 677L);
		public static final int subtractExact_int_int_int_1 = Math.subtractExact(299, 897);
		public static final long subtractExact_long_long_long_1 = Math.subtractExact(382L, 641L);
		public static final int multiplyExact_int_int_int_1 = Math.multiplyExact(978, 974);
		public static final long multiplyExact_long_int_long_1 = Math.multiplyExact(349L, 480);
		public static final long multiplyExact_long_long_long_1 = Math.multiplyExact(179L, 970L);
		public static final int incrementExact_int_int_1 = Math.incrementExact(896);
		public static final long incrementExact_long_long_1 = Math.incrementExact(513L);
		public static final int decrementExact_int_int_1 = Math.decrementExact(903);
		public static final long decrementExact_long_long_1 = Math.decrementExact(946L);
		public static final int negateExact_int_int_1 = Math.negateExact(421);
		public static final long negateExact_long_long_1 = Math.negateExact(853L);
		public static final int toIntExact_long_int_1 = Math.toIntExact(270L);
		public static final int floorDiv_int_int_int_1 = Math.floorDiv(941, 797);
		public static final long floorDiv_long_int_long_1 = Math.floorDiv(653L, 969);
		public static final long floorDiv_long_long_long_1 = Math.floorDiv(320L, 425L);
		public static final int floorMod_int_int_int_1 = Math.floorMod(848, 105);
		public static final long floorMod_long_long_long_1 = Math.floorMod(445L, 254L);
		public static final int abs_int_int_1 = Math.abs(516);
		public static final long abs_long_long_1 = Math.abs(681L);
		public static final float abs_float_float_1 = Math.abs(0.65483654f);
		public static final double abs_double_double_1 = Math.abs(0.4528232837825946d);
		public static final int max_int_int_int_1 = Math.max(51, 534);
		public static final long max_long_long_long_1 = Math.max(364L, 682L);
		public static final float max_float_float_float_1 = Math.max(0.9257008f, 0.6501638f);
		public static final double max_double_double_double_1 = Math.max(0.43926012767436695d, 1.1093812539607837d);
		public static final int min_int_int_int_1 = Math.min(339, 204);
		public static final long min_long_long_long_1 = Math.min(430L, 877L);
		public static final float min_float_float_float_1 = Math.min(1.0570978f, 1.2506227f);
		public static final double min_double_double_double_1 = Math.min(1.101469006601385d, 0.6307553677726297d);
		public static final double ulp_double_double_1 = Math.ulp(0.491960835280729d);
		public static final float ulp_float_float_1 = Math.ulp(0.8281953f);
		public static final double signum_double_double_1 = Math.signum(0.9999574293591595d);
		public static final float signum_float_float_1 = Math.signum(1.0379215f);
		public static final double sinh_double_double_1 = Math.sinh(0.42632238065394956d);
		public static final double cosh_double_double_1 = Math.cosh(0.36848676239971d);
		public static final double tanh_double_double_1 = Math.tanh(1.145821170642709d);
		public static final double hypot_double_double_double_1 = Math.hypot(1.2037126998861707d, 0.756380218300418d);
		public static final double expm1_double_double_1 = Math.expm1(0.7614147490623953d);
		public static final double log1p_double_double_1 = Math.log1p(1.0665935656093923d);
		public static final double copySign_double_double_double_1 = Math.copySign(0.6669942457624539d,
				1.0770778267358403d);
		public static final float copySign_float_float_float_1 = Math.copySign(0.52208996f, 1.0889753f);
		public static final int getExponent_float_int_1 = Math.getExponent(0.9562948f);
		public static final int getExponent_double_int_1 = Math.getExponent(1.1399933542443466d);
		public static final double nextAfter_double_double_double_1 = Math.nextAfter(1.282151540186312d,
				1.1106238551081802d);
		public static final float nextAfter_float_double_float_1 = Math.nextAfter(0.8473394f, 1.2972115400091506d);
		public static final double nextUp_double_double_1 = Math.nextUp(0.5729738224093295d);
		public static final float nextUp_float_float_1 = Math.nextUp(0.88414603f);
		public static final double nextDown_double_double_1 = Math.nextDown(1.2907127749369773d);
		public static final float nextDown_float_float_1 = Math.nextDown(0.96942246f);
		public static final double scalb_double_int_double_1 = Math.scalb(0.47741444756518225d, 690);
		public static final float scalb_float_int_float_1 = Math.scalb(0.97882897f, 640);

		//since Java 9
		//TODO test these as well
//		public static final double fma_double_double_double_double_1 = Math.fma(0.9498999081237168d, 0.41288459997088234d, 1.3364649394490504d);
//		public static final float fma_float_float_float_float_1 = Math.fma(1.241152f, 1.3250695f, 0.9307948f);
//		public static final int floorMod_long_int_int_1 = Math.floorMod(73L, 160);
//		public static final long multiplyFull_int_int_long_1 = Math.multiplyFull(885, 300);
//		public static final long multiplyHigh_long_long_long_1 = Math.multiplyHigh(689L, 825L);
	}
}
