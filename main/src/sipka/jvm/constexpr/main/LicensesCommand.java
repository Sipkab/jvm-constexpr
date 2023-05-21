package sipka.jvm.constexpr.main;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.jar.Manifest;

import sipka.jvm.constexpr.tool.Utils;

/**
 * <pre>
 * Prints the licenses of the included software used by jvm-constexpr.
 * </pre>
 */
public class LicensesCommand {
	// our own attribute name instead of deriving it from Nest-Bundle-Identifier
	// because we don't want to deal with its format (even if its straightforward)
	private static final String MANIFEST_VERSION_ATTRIBUTE_NAME = "Sipka-Jvm-Constexpr-Version";

	public void call() throws Exception {
		ClassLoader classloader = CliMain.class.getClassLoader();
		PrintStream out = System.out;

		String version = "<unknown>";
		try (InputStream in = classloader.getResourceAsStream("META-INF/MANIFEST.MF")) {
			if (in != null) {
				//just in case, the code might get bundled a different way
				Manifest mf = new Manifest(in);
				version = mf.getMainAttributes().getValue(MANIFEST_VERSION_ATTRIBUTE_NAME);
			}
		}

		out.println("jvm-constexpr version " + version);
		out.println("Copyright (C) 2023 Bence Sipka");
		out.println();
		try (InputStream in = classloader.getResourceAsStream("META-INF/LICENSE")) {
			if (in != null) {
				Utils.copyStream(in, out);
			}
		}
		out.println();
		out.println("----------");
		out.println("Embedded software licenses");
		out.println();
		try (InputStream in = classloader.getResourceAsStream("META-INF/BUNDLED-LICENSES")) {
			if (in != null) {
				Utils.copyStream(in, out);
			}
		}
	}
}
