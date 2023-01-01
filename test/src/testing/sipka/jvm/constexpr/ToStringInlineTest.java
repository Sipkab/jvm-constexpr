package testing.sipka.jvm.constexpr;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import sipka.jvm.constexpr.tool.log.IndeterministicToStringLogEntry;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.ClassNode;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.tree.FieldNode;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

/**
 * Tests that {@link Object#toString()} is correctly inlined when appropriate
 */
@SakerTest
public class ToStringInlineTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		{
			InlinerOptions opts = TestUtils.createOptionsForClasses(AllInlined.class);
			opts.setConstantTypes(Arrays.asList(MyConstantType.class));
			opts.setConstantFields(Arrays.asList(AllInlined.class.getDeclaredField("random_FORCEOBJSTR"),
					AllInlined.class.getDeclaredField("random_FORCEARRAYSTR"),
					AllInlined.class.getDeclaredField("random_FORCENONCONSTTYPESTR")));
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			TestUtils.assertSameStaticFieldValues(classnode, AllInlined.class);
			assertNull(TestUtils.getClInitMethod(classnode), "clinit method");

			//check that they've been logged
			Set<? extends IndeterministicToStringLogEntry> tostringlogentries = ((TestCollectingLogger) opts
					.getLogger()).getLogEntriesForType(IndeterministicToStringLogEntry.class);
			assertNotEmpty(tostringlogentries);
			assertEquals(
					tostringlogentries.stream().map(IndeterministicToStringLogEntry::getClassInternalName)
							.collect(Collectors.toSet()),
					new TreeSet<>(Arrays.asList(Type.getInternalName(int[].class), Type.getInternalName(Object.class),
							Type.getInternalName(MyConstantType.class),
							Type.getInternalName(MyNonConstantType.class))));
		}
		{
			InlinerOptions opts = TestUtils.createOptionsForClasses(NothingInlined.class);
			opts.setConstantTypes(Arrays.asList(MyConstantType.class));
			opts.setConstantReconstructors(Arrays.asList(MyNonConstantType.class.getConstructor()));
			NavigableMap<String, ClassNode> outputs = TestUtils.performInliningClassNodes(opts);
			assertEquals(outputs.size(), 1); // testing a single class

			ClassNode classnode = outputs.firstEntry().getValue();
			for (FieldNode fn : TestUtils.getFields(classnode).values()) {
				assertNull(fn.value);
			}
		}
	}

	public static class NothingInlined {
		public static final String OBJSTR = new Object().toString();
		public static final String ARRAYSTR = new int[3].toString();
		public static final String ARRAYOBJSTR = ((Object) new int[3]).toString();
		public static final String NULLSTR = ((Object) null).toString();
		public static final String NONCONSTTYPESTR = new MyNonConstantType().toString();
	}

	public static class AllInlined {
		public static final String STRSTR = "abc".toString();
		public static final String CHARSEQSTR = ((CharSequence) "abc").toString();
		public static final String OBJSTRSTR = ((Object) "abc").toString();
		public static final String INTSTR = Integer.valueOf(123).toString();
		public static final String ENUMSTR = DayOfWeek.FRIDAY.toString();
		//random_ prefix to avoid test function checking its equality
		//this should be inlined, but not deterministic
		public static final String random_CONSTTYPESTR = new MyConstantType().toString();
		public static final String random_FORCEOBJSTR = new Object().toString();
		public static final String random_FORCEARRAYSTR = new int[3].toString();
		public static final String random_FORCENONCONSTTYPESTR = new MyNonConstantType().toString();
	}

	public static class MyConstantType {
	}

	public static class MyNonConstantType {

	}
}
