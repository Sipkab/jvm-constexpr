package testing.sipka.jvm.constexpr;

import java.util.Map;

import sipka.jvm.constexpr.main.AnnotationAnalyzer;
import testing.saker.SakerTest;
import testing.saker.SakerTestCase;

@SakerTest
public class GetterNameSearchTest extends SakerTestCase {

	@Override
	public void runTest(Map<String, String> parameters) throws Throwable {
		assertEmpty(AnnotationAnalyzer.searchGetter(new Object() {
			Object myMessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage"));
		assertEquals(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence myMessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage").size(), 1);
		assertEmpty(AnnotationAnalyzer.searchGetter(new Object() {
			String myMessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage"));

		assertEquals(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence mymessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage").size(), 1);
		assertEquals(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence getmymessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage").size(), 1);
		assertEquals(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence getMyMessage() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage").size(), 1);

		assertEquals(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence GETMYMESSAGE() {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage").size(), 1);

		assertEmpty(AnnotationAnalyzer.searchGetter(new Object() {
			CharSequence getMyMessage(int index) {
				return null;
			}
		}.getClass(), CharSequence.class, "myMessage"));
	}
}
