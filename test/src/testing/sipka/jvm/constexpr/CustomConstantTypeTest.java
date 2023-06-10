package testing.sipka.jvm.constexpr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests some custom constant type related functionality.
 */
@SakerTest
public class CustomConstantTypeTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		opts.setConstantTypes(Arrays.asList(MyConstantClass.class));

		Map<Class<?>, DeconstructionSelector> deconstructorConfigurations = new HashMap<>();
		deconstructorConfigurations.put(MyConstantClass.class,
				DeconstructionSelector.getForConfiguration(
						DeconstructorConfiguration.createConstructor(MyConstantClass.class.getConstructor(int.class),
								DeconstructionDataAccessor.createForMethod(MyConstantClass.class, "getVal"))));
		opts.setDeconstructorConfigurations(deconstructorConfigurations);

		opts.setConstantReconstructors(TestUtils.allowAllMembers(Arrays.asList(
				MyConstantClass.class.getMethod("create", int.class), MyConstantClass.class.getMethod("hashCode"))));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, MyConstantClass.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);
		//the reloaded class should be deconstructed using the factory method
		assertEquals(TestUtils.readFieldOfStaticField(loadedclass, "CC1", "source"), "constructor");
		assertEquals(Constants.CC1.source, "factory");
	}

	public static class Constants {
		public static final MyConstantClass CC1 = MyConstantClass.create(123);
		public static final MyConstantClass CC2 = MyConstantClass.create(654);
		public static final int VAL1 = CC1.getVal();
		public static final int VAL2 = CC2.getVal();
		public static final int ADD = CC1.getVal() + CC2.getVal();
		
		public static final int CMP1 = new MyConstantClass(123).compareTo(new MyConstantClass(456));
		public static final int CMP2 = CC1.compareTo(new MyConstantClass(456));
		public static final int CMP3 = CC2.compareTo(new MyConstantClass(CMP2));

		public static final int HASHCODE1 = CC1.hashCode();

		public static final MyConstantClass MULTIPATH;
		static {
			if (Boolean.getBoolean("test")) {
				MULTIPATH = MyConstantClass.create(123);
			} else {
				MULTIPATH = MyConstantClass.create(123);
			}
		}

	}

	public static class MyConstantClass implements Comparable<MyConstantClass> {
		public int val;
		public transient String source;

		public MyConstantClass(int val) {
			this.val = val;
			source = "constructor";
		}

		public static MyConstantClass create(int val) {
			MyConstantClass result = new MyConstantClass(val);
			result.source = "factory";
			return result;
		}

		public int getVal() {
			return val;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + val;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyConstantClass other = (MyConstantClass) obj;
			if (val != other.val)
				return false;
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MyConstantClass[val=");
			builder.append(val);
			builder.append(", source=");
			builder.append(source);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int compareTo(MyConstantClass o) {
			return Integer.compare(this.val, o.val);
		}

	}
}
