package testing.sipka.jvm.constexpr.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.annotations.Deconstructor;
import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

@SakerTest
public class FieldDeconstructorCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path injarpath = testCaseDirectory.resolve("classes.jar");
		Path outjarpath = testCaseDirectory.resolve("out.jar");
		TestUtils.writeJar(injarpath, Constants.class, CliSimpleConstantType.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-output", outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());

		//hashcode still present in clinit
		assertNonNull(TestUtils.getClInitMethod(classnode), "clinit method");

		ClassLoader outcl = TestUtils.createJarClassLoader(outjarpath);
		Object INIT_ZERO_value = Class.forName(Constants.class.getName(), false, outcl).getField("INIT_ZERO").get(null);
		Object ZERO_value = Class.forName(CliSimpleConstantType.class.getName(), false, outcl).getField("ZERO")
				.get(null);
		//should identity equal
		assertIdentityEquals(INIT_ZERO_value, ZERO_value, "zero instance equality");
		assertEquals(INIT_ZERO_value.getClass().getField("source").get(INIT_ZERO_value), "ZERO", "INIT_ZERO.source optimized");
		assertEquals(Constants.INIT_ZERO.source, "constructor", "INIT_ZERO.source current");

		Object INIT_ONE_value = Class.forName(Constants.class.getName(), false, outcl).getField("INIT_ONE").get(null);
		assertEquals(INIT_ONE_value.getClass().getField("value").get(INIT_ONE_value), 1, "INIT_ONE.value");
	}

	public static class Constants {

		public static final CliSimpleConstantType INIT_ZERO = new CliSimpleConstantType(0);
		public static final CliSimpleConstantType INIT_ONE = new CliSimpleConstantType(1);
	}

	@ConstantExpression
	public static class CliSimpleConstantType {
		@Deconstructor
		public static final CliSimpleConstantType ZERO = new CliSimpleConstantType(0);
		static {
			ZERO.source = "ZERO";
		}

		public int value;
		public transient String source;

		public CliSimpleConstantType(int value) {
			this.value = value;
			this.source = "constructor";
		}

		public CliSimpleConstantType(CliSimpleConstantType other) {
			this.value = other.value;
			this.source = "copy-constructor";
		}

		public static CliSimpleConstantType makeNonDeconstructor(int value) {
			CliSimpleConstantType result = new CliSimpleConstantType(value);
			result.source = "makeNonDeconstructor";
			return result;
		}

		@ConstantExpression
		public static CliSimpleConstantType make(int value) {
			CliSimpleConstantType result = new CliSimpleConstantType(value);
			result.source = "make";
			return result;
		}

		@ConstantExpression
		public static CliSimpleConstantType makeMult(int value, int mult) {
			CliSimpleConstantType result = new CliSimpleConstantType(value * mult);
			result.source = "makeMult";
			return result;
		}

		public int getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			//unstable hashcode, just to check that its not inlined
			return value * getClass().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CliSimpleConstantType other = (CliSimpleConstantType) obj;
			if (value != other.value)
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CliSimpleConstantType[value=");
			builder.append(value);
			builder.append("]");
			return builder.toString();
		}

	}
}
