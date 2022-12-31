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
		TestUtils.writeJar(injarpath, Constants.class, CliSimpleConstantType.class, CacheFields.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-output", outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());

		//hashcode still present in clinit
		assertNonNull(TestUtils.getClInitMethod(classnode), "clinit method");

		ClassLoader outcl = TestUtils.createJarClassLoader(outjarpath);
		Class<?> asmcontstantsclass = Class.forName(Constants.class.getName(), false, outcl);
		Class<?> asmconstanttypeclass = Class.forName(CliSimpleConstantType.class.getName(), false, outcl);

		Object INIT_ZERO = asmcontstantsclass.getField("INIT_ZERO").get(null);
		Object ZERO = asmconstanttypeclass.getField("ZERO").get(null);
		//should identity equal
		assertIdentityEquals(INIT_ZERO, ZERO, "zero instance equality");
		assertEquals(INIT_ZERO.getClass().getField("source").get(INIT_ZERO), "ZERO", "INIT_ZERO.source optimized");
		assertEquals(Constants.INIT_ZERO.source, "constructor", "INIT_ZERO.source current");

		Object INIT_ONE = asmcontstantsclass.getField("INIT_ONE").get(null);
		assertEquals(INIT_ONE.getClass().getField("value").get(INIT_ONE), 1, "INIT_ONE.value");

		Object INIT_HUNDRED = asmcontstantsclass.getField("INIT_HUNDRED").get(null);
		assertEquals(asmconstanttypeclass.getField("value").get(INIT_HUNDRED), 100, "INIT_HUNDRED.value");
		assertEquals(asmconstanttypeclass.getField("source").get(INIT_HUNDRED), "HUNDRED", "INIT_HUNDRED.source");
	}

	public static class Constants {

		public static final CliSimpleConstantType INIT_ZERO = new CliSimpleConstantType(0);
		public static final CliSimpleConstantType INIT_ONE = new CliSimpleConstantType(1);
		public static final CliSimpleConstantType INIT_HUNDRED = new CliSimpleConstantType(100);
	}

	public static class CacheFields {
		@Deconstructor
		public static final CliSimpleConstantType HUNDRED = new CliSimpleConstantType(100);
		static {
			HUNDRED.source = "HUNDRED";
		}
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
