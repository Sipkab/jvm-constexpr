package sipka.jvm.constexpr.main;

import java.io.InputStream;

import sipka.jvm.constexpr.tool.Utils;

/**
 * <pre>
 * Prints the licenses of the included software used by jvm-constexpr.
 * </pre>
 */
public class LicensesCommand {
	public void call() throws Exception {
		System.out.println("jvm-constexpr version " /*TODO set version*/);
		System.out.println("Copyright (C) 2022 Bence Sipka");
		System.out.println();
		try (InputStream in = CliMain.class.getClassLoader().getResourceAsStream("META-INF/LICENSE")) {
			Utils.copyStream(in, System.out);
		}
		System.out.println();
		System.out.println("----------");
		System.out.println("Embedded software licenses");
		System.out.println();
		try (InputStream in = CliMain.class.getClassLoader().getResourceAsStream("META-INF/BUNDLED-LICENSES")) {
			Utils.copyStream(in, System.out);
		}
	}
}
