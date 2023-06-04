package testing.sipka.jvm.constexpr;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;

import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class ConfigFileTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		InlinerOptions opts = TestUtils.createOptionsForClasses(Constants.class);
		opts.setConfigFiles(Arrays.asList(Paths.get("test/resources/" + getClass().getSimpleName() + "/asm_config")));

		NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
		assertEquals(outputs.size(), 1); // testing a single class

		ClassNode classnode = outputs.firstEntry().getValue();

		TestUtils.assertSameStaticFieldValues(classnode, Constants.class);
	}

	public static class Constants {
		public static final String STR = Type.getInternalName(Object.class);

	}
}
