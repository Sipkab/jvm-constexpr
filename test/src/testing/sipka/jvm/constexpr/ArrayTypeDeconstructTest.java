package testing.sipka.jvm.constexpr;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.thirdparty.saker.util.ReflectUtils;
import sipka.jvm.constexpr.tool.log.ReconstructionFailureLogEntry;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Test array field type related functionality.
 */
@SakerTest
public class ArrayTypeDeconstructTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);

		List<Member> reconstructors = new ArrayList<>();
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "returnSame", OtherClass[].class));
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "returnClone", OtherClass[].class));
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "returnSameObject", Object[].class));
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "returnCloneObject", Object[].class));
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "length", OtherClass[].class));
		reconstructors.add(ReflectUtils.getDeclaredMethodAssert(Constants.class, "makelen", int.class));
		opts.setConstantReconstructors(reconstructors);

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();
		Class<?> loadedclass = Class.forName(Constants.class.getName(), false,
				TestUtils.createClassLoader(classnode, OtherClass.class));
		TestUtils.assertSameStaticFieldValues(classnode, Constants.class, loadedclass);

		assertEmpty(
				((TestCollectingLogger) opts.getLogger()).getLogEntriesForType(ReconstructionFailureLogEntry.class));
	}

	public static class OtherClass {
	}

	public static class Constants {
		public static final OtherClass[] newarray = new OtherClass[3];
		public static final OtherClass[] returned = returnSame(new OtherClass[4]);
		public static final int len = length(new OtherClass[5]);
		public static final OtherClass[] makelen = makelen(6);
		public static final int makelenLen = makelen(7).length;
		public static final OtherClass[] cloned = returnClone(new OtherClass[8]);
		public static final int clonedLen = returnClone(new OtherClass[9]).length;

		public static final OtherClass[] returnedObj = returnSameObject(new OtherClass[10]);
		public static final OtherClass[] clonedObj = returnCloneObject(new OtherClass[11]);

		public static OtherClass[] returnSame(OtherClass[] input) {
			return input;
		}

		public static OtherClass[] returnClone(OtherClass[] input) {
			return input.clone();
		}

		public static OtherClass[] returnSameObject(Object[] input) {
			return (OtherClass[]) input;
		}

		public static OtherClass[] returnCloneObject(Object[] input) {
			return (OtherClass[]) input.clone();
		}

		public static int length(OtherClass[] input) {
			return input.length;
		}

		public static OtherClass[] makelen(int len) {
			return new OtherClass[len];
		}
	}
}
