package testing.sipka.jvm.constexpr.cli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.main.CliMain;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.sipka.jvm.constexpr.TestUtils;

@SakerTest
public class SimpleConstantTypeCliTest extends CliTestCase {

	@Override
	protected void runTestImpl(Map<String, String> parameters) throws Throwable {
		Path injarpath = testCaseDirectory.resolve("classes.jar");
		Path outjarpath = testCaseDirectory.resolve("out.jar");
		TestUtils.writeJar(injarpath, Constants.class, CliSimpleConstantType.class, RuntimeTestAnnot.class);

		Files.deleteIfExists(outjarpath);

		CliMain.main("-input", injarpath.toString(), "-output", outjarpath.toString());

		ClassNode classnode = TestUtils.loadClassNodeFromJar(outjarpath, Constants.class.getName());
		assertEquals(TestUtils.getFields(classnode).get("METHODVAL").value, 10, "RECONS.value");
		assertEquals(TestUtils.getFields(classnode).get("FIELDVAL").value, 10, "RECONS.value");
		assertNull(TestUtils.getFields(classnode).get("HASHCODE").value, "HASHCODE.value");
		assertNull(TestUtils.getFields(classnode).get("DECONS").value, "DECONS.value");
		assertEquals(TestUtils.getFields(classnode).get("RECONS").value, 888, "RECONS.value");
		assertEquals(TestUtils.getFields(classnode).get("RECONSMULT").value, 777 * 666, "RECONSMULT.value");

		//hashcode still present in clinit
		assertNonNull(TestUtils.getClInitMethod(classnode), "clinit method");

		ClassLoader outcl = TestUtils.createJarClassLoader(outjarpath);
		Object INIT_ZERO_value = Class.forName(Constants.class.getName(), false, outcl).getField("INIT_ZERO").get(null);
		Class<?> cttypeclass = Class.forName(CliSimpleConstantType.class.getName(), false, outcl);
		Object ZERO_value = cttypeclass.getField("ZERO").get(null);
		assertNotIdentityEquals(INIT_ZERO_value, ZERO_value, "zero instance equality");

		TestUtils.assertJarAnnotationsRemoved(outjarpath);

		assertEquals(cttypeclass.getAnnotations().length, 1);
		assertEquals(cttypeclass.getField("ZERO").getAnnotations().length, 1);
		assertEquals(cttypeclass.getConstructor(int.class).getAnnotations().length, 1);
		assertEquals(cttypeclass.getConstructor(cttypeclass).getAnnotations().length, 0);
	}

	public static class Constants {
		public static final int METHODVAL = new CliSimpleConstantType(10).getValue();
		public static final int FIELDVAL = new CliSimpleConstantType(10).value;
		public static final int HASHCODE = new CliSimpleConstantType(123).hashCode();
		//cannot be inlined, because makeNonDeconstructor is not a reconstructor
		public static final int DECONS = CliSimpleConstantType.makeNonDeconstructor(999).value;

		public static final int RECONS = CliSimpleConstantType.make(888).value;
		public static final int RECONSMULT = CliSimpleConstantType.makeMult(777, 666).value;

		public static final CliSimpleConstantType INIT_ZERO = new CliSimpleConstantType(0);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
	public @interface RuntimeTestAnnot {

	}

	@ConstantExpression
	@RuntimeTestAnnot // check that this is not stripped
	public static class CliSimpleConstantType {

		@RuntimeTestAnnot // check that this is not stripped
		public static final CliSimpleConstantType ZERO = new CliSimpleConstantType(0);
		static {
			ZERO.source = "ZERO";
		}

		public int value;
		public transient String source;

		@RuntimeTestAnnot // check that this is not stripped
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
