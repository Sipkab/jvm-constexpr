package sipka.jvm.constexpr.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import sipka.cmdline.api.Flag;
import sipka.cmdline.api.MultiParameter;
import sipka.cmdline.api.Parameter;
import sipka.jvm.constexpr.annotations.ConstantExpression;
import sipka.jvm.constexpr.annotations.Deconstructor;
import sipka.jvm.constexpr.tool.ConstantExpressionInliner;
import sipka.jvm.constexpr.tool.OutputConsumer;
import sipka.jvm.constexpr.tool.Utils;
import sipka.jvm.constexpr.tool.log.LogEntry;
import sipka.jvm.constexpr.tool.log.ToolLogger;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Type;

/**
 * <pre>
 * Performs constant expression optimization on the specified Java input class files.
 * </pre>
 */
public class RunCommand {
	/**
	 * <pre>
	 * The input classes to process. Allows setting class files, directories,
	 * ZIP and JAR files as inputs.
	 *
	 * Can be specified multiple times, also with ; as path separator.
	 * </pre>
	 */
	@Parameter(value = { "-input" }, required = true)
	@MultiParameter(String.class)
	public Collection<String> input = new LinkedHashSet<>();

	/**
	 * <pre>
	 * The additional classpath that are loaded along the -input, but these
	 * classes are not optimized.
	 * 
	 * Allows the same kind of arguments as -input.
	 * </pre>
	 */
	@Parameter(value = { "-classpath", "-cp" }, required = false)
	@MultiParameter(String.class)
	public Collection<String> classpath = new LinkedHashSet<>();

	/**
	 * <pre>
	 * Flag to specify if the input files should be overwritten by the output.
	 * 
	 * Can't be set alongside -output.
	 * </pre>
	 */
	@Parameter("-overwrite")
	@Flag
	public boolean overwrite;

	/**
	 * <pre>
	 * Specifies the output of the optimization.
	 * 
	 * May be a directory, .zip, or .jar files.
	 * Can't be set alongside -overwrite.
	 * 
	 * If no -output (or -overwrite) is set, the command will perform a dry run. 
	 * The possible optimizations are logged, but the results are not written to the storage.
	 * </pre>
	 */
	@Parameter(value = { "-output" })
	public String output;

	/**
	 * <pre>
	 * Specifies config files to load.
	 * 
	 * The config files contain information about additional elements that can be
	 * used for constant optimization.
	 * </pre>
	 */
	@Parameter(value = { "-config" }, required = false)
	@MultiParameter(String.class)
	public Collection<String> configFiles = new LinkedHashSet<>();

	private boolean outputZip;
	private boolean outputDir;

	private Path outputDirectory;

	/**
	 * Map input ZIP/JAR path to output ZIP entry bytes in {@link #overwrite} mode.
	 */
	private Map<Path, Map<String, byte[]>> overwriteZipFileOutputBytes = new TreeMap<>();
	/**
	 * Maps class file paths to their output bytes.
	 * <p>
	 * If overwriting class files, or writing to an output directory.
	 */
	private Map<Path, byte[]> outputPathBytes = new TreeMap<>();
	/**
	 * Maps output zip entry names to their bytes.
	 * <p>
	 * If the output is a single zip file.
	 */
	//use LinkedHashMap to keep the order as it was read on the classpath
	private Map<String, ZipEntryBytes> outputZipEntryBytes = new LinkedHashMap<>();

	public void call() throws Exception {
		if (overwrite && output != null) {
			throw new IllegalArgumentException(
					"Both -overwrite and -output were specified. Only one is allowed at a time.");
		}
		Path outputpath;
		if (output != null) {
			outputpath = Paths.get(output).toAbsolutePath().normalize();
			String outputpathfilename = outputpath.getFileName().toString();
			outputZip = outputpathfilename.endsWith(".jar") || outputpathfilename.endsWith(".zip");
			if (Files.isDirectory(outputpath)) {
				if (outputZip) {
					throw new FileAlreadyExistsException(outputpath.toString(), null,
							"Output already exists as a directory.");
				}
				outputDir = true;
				outputDirectory = outputpath;
			} else {
				outputDir = !outputZip;
				if (outputDir) {
					outputDirectory = outputpath;
				}
			}
		} else {
			outputpath = null;
		}

		Collection<URL> classloaderurls = new LinkedHashSet<>();
		Collection<Path> classpathpaths = new LinkedHashSet<>();
		Collection<Path> inputpaths = new LinkedHashSet<>();
		LinkedHashSet<Path> configfilepaths = new LinkedHashSet<>();
		toPaths(classpathpaths, classpath);
		toPaths(inputpaths, input);
		toPaths(configfilepaths, configFiles);
		for (Path clpath : classpathpaths) {
			classloaderurls.add(clpath.toUri().toURL());
		}
		for (Path inpath : inputpaths) {
			if (!classloaderurls.add(inpath.toUri().toURL())) {
				throw new IllegalArgumentException("Duplicate Path in classpath and input: " + inpath);
			}
		}
		InlinerOptions options = createBaseOptions();

		options.setConfigFiles(configfilepaths);

		//strip the constant annotations by default
		Set<String> stripannots = new TreeSet<>();
		stripannots.add(Type.getInternalName(ConstantExpression.class));
		stripannots.add(Type.getInternalName(Deconstructor.class));
		options.setStripAnnotations(stripannots);

		//scan the complete classpath for annotations
		Map<String, ClassBytes> classestoanalyze = new LinkedHashMap<>();
		for (Path path : classpathpaths) {
			scanClasspath(options, path, classestoanalyze, false);
		}
		for (Path path : inputpaths) {
			scanClasspath(options, path, classestoanalyze, true);
		}

		try (InputLoadingURLClassLoader cl = new InputLoadingURLClassLoader(classloaderurls.toArray(new URL[0]),
				getParentClassLoader())) {
			for (Entry<String, ClassBytes> entry : classestoanalyze.entrySet()) {
				//add the individually specified class files from the classpath to the classloader
				ClassBytes cbytes = entry.getValue();
				if (cbytes.individualClassFile) {
					cl.addClassFile(Type.getObjectType(entry.getKey()).getClassName(), cbytes.bytes);
				}
			}

			options.setClassLoader(cl);

			AnnotationAnalyzer.analyzeClassFile(options,
					classestoanalyze.values().stream().map(cb -> cb.bytes)::iterator);

			ConstantExpressionInliner.run(options);
		}

		for (Entry<Path, byte[]> entry : outputPathBytes.entrySet()) {
			Path classfilepath = entry.getKey();
			Files.createDirectories(classfilepath.getParent());
			Files.write(classfilepath, entry.getValue());
		}
		if (!overwriteZipFileOutputBytes.isEmpty()) {
			//overwrite the ZIP files
			for (Entry<Path, Map<String, byte[]>> entry : overwriteZipFileOutputBytes.entrySet()) {
				Path inputzippath = entry.getKey();
				Path tempfile = inputzippath.getParent()
						.resolve(inputzippath.getFileName() + "_temp-" + UUID.randomUUID());
				Map<String, byte[]> outputentries = entry.getValue();
				try {
					try (InputStream is = Files.newInputStream(inputzippath);
							ZipInputStream zis = new ZipInputStream(is);
							OutputStream os = Files.newOutputStream(tempfile, StandardOpenOption.CREATE_NEW);
							ZipOutputStream zos = new ZipOutputStream(os)) {
						for (ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
							byte[] outputbytes = outputentries.get(ze.getName());
							ZipEntry nze = cloneZipEntry(ze);
							zos.putNextEntry(nze);
							if (outputbytes != null) {
								zos.write(outputbytes);
							} else {
								Utils.copyStream(zis, zos);
							}
							zos.closeEntry();
						}
					}
					//replace the input zip with the new one
					Files.move(tempfile, inputzippath, StandardCopyOption.REPLACE_EXISTING);
				} finally {
					Files.deleteIfExists(tempfile);
				}
			}
		}

		if (outputZip) {
			//outputting everything as a single zip
			Files.createDirectories(outputpath.getParent());

			try (OutputStream fos = Files.newOutputStream(outputpath);
					ZipOutputStream zos = new ZipOutputStream(fos)) {
				for (Entry<String, ZipEntryBytes> entry : outputZipEntryBytes.entrySet()) {
					ZipEntryBytes entrybytes = entry.getValue();
					zos.putNextEntry(entrybytes.zipEntry);
					zos.write(entrybytes.bytes);
					zos.closeEntry();
				}
			}
		}
	}

	private static InlinerOptions createBaseOptions() {
		return createBaseOptions(System.out);
	}

	private static InlinerOptions createBaseOptions(PrintStream outstream) {
		InlinerOptions options = new InlinerOptions();
		options.setLogger(new PrintStreamToolLogger(outstream));
		options.setOutputConsumer(OutputHandlerForwardingOutputConsumer.INSTANCE);
		return options;
	}

	private static ClassLoader getParentClassLoader() {
		//available from JDK 9+
		try {
			return (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
		} catch (Exception e) {
			//okay, if we're running on Java 8
			return null;
		}
	}

	private void scanClasspath(InlinerOptions options, Path path, Map<String, ClassBytes> classestoanalyze,
			boolean input) throws Exception {
		if (!Files.exists(path)) {
			throw new NoSuchFileException(path.toString());
		}
		if (Files.isDirectory(path)) {
			try (Stream<Path> walkstream = Files.walk(path)) {
				for (Iterator<Path> it = walkstream.iterator(); it.hasNext();) {
					Path p = it.next();
					if (Files.isDirectory(p)) {
						continue;
					}
					byte[] bytes = Files.readAllBytes(p);
					if (input && outputZip) {
						ZipEntry ze = new ZipEntry(path.relativize(p).toString());
						ze.setLastModifiedTime(Files.getLastModifiedTime(p));
						outputZipEntryBytes.put(ze.getName(), new ZipEntryBytes(ze, bytes));
					}
					if (p.getFileName().toString().endsWith(".class")) {
						ClassReader cr = new ClassReader(bytes);
						String classinternalname = cr.getClassName();
						classestoanalyze.put(classinternalname, new ClassBytes(p, bytes));

						if (input) {
							OutputHandler handler;
							if (overwrite) {
								handler = new ClassFileOutputHandler(p);
							} else {
								handler = getOutputHandler(classinternalname, p, null);
							}
							options.getInputs().add(ToolInput.createForPath(handler, p));
						}
					}
				}
			}
		} else {
			String pathfilename = path.getFileName().toString();
			if (pathfilename.endsWith(".class")) {
				//simple class, not zip or jar

				byte[] bytes = Files.readAllBytes(path);

				ClassReader cr = new ClassReader(bytes);
				String classinternalname = cr.getClassName();
				ClassBytes classbytes = new ClassBytes(path, bytes);
				classbytes.individualClassFile = true;
				classestoanalyze.put(classinternalname, classbytes);

				if (input) {
					if (outputZip) {
						ZipEntry ze = new ZipEntry(classinternalname + ".class");
						ze.setLastModifiedTime(Files.getLastModifiedTime(path));
						outputZipEntryBytes.put(ze.getName(), new ZipEntryBytes(ze, bytes));
					}
					OutputHandler handler;
					if (overwrite) {
						handler = new ClassFileOutputHandler(path);
					} else {
						handler = getOutputHandler(classinternalname, path, null);
					}
					options.getInputs().add(ToolInput.createForPath(handler, path));
				}
			} else {
				try (InputStream is = Files.newInputStream(path);
						ZipInputStream zis = pathfilename.endsWith(".jar") ? new JarInputStream(is)
								: new ZipInputStream(is)) {
					if (input && overwrite) {
						//init the output map of the overwriting archive
						//use linked map to keep the entry order
						overwriteZipFileOutputBytes.put(path, new LinkedHashMap<>());
					}
					for (ZipEntry ze; (ze = zis.getNextEntry()) != null;) {
						byte[] bytes = Utils.readStream(zis);

						if (input && outputZip) {
							outputZipEntryBytes.put(ze.getName(), new ZipEntryBytes(cloneZipEntry(ze), bytes));
						}
						if (ze.getName().endsWith(".class")) {
							ClassReader cr = new ClassReader(bytes);
							String classinternalname = cr.getClassName();
							ClassBytes classbytes = new ClassBytes(path, bytes);
							classbytes.zipEntry = ze;
							classestoanalyze.put(classinternalname, classbytes);

							OutputHandler handler;
							if (overwrite) {
								handler = new OverwriteZipOutputHandler(path, ze.getName());
							} else {
								handler = getOutputHandler(classinternalname, null, ze);
							}
							if (input) {
								if (ze.getName().startsWith("META-INF/versions/")) {
									//not supported (yet?)
									throw new IllegalArgumentException("Multi-release JARs are not supported: " + path);
								}
								options.getInputs().add(ToolInput.createWithBytes(handler, bytes));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * @param classinternalname
	 * @param filepath
	 *            If the input is a file.
	 * @param zipentry
	 *            If the input is a zip entry.
	 * @return
	 * @throws IOException
	 */
	private OutputHandler getOutputHandler(String classinternalname, Path filepath, ZipEntry zipentry)
			throws IOException {
		if (outputDir) {
			return new ClassFileOutputHandler(outputDirectory.resolve(classinternalname + ".class"));
		}
		if (outputZip) {
			ZipEntry ze;
			if (zipentry != null) {
				ze = cloneZipEntry(zipentry);
			} else {
				ze = new ZipEntry(classinternalname + ".class");
				ze.setLastModifiedTime(Files.getLastModifiedTime(filepath));
			}
			return new ZipOutputHandler(ze);
		}
		return null;
	}

	private static ZipEntry cloneZipEntry(ZipEntry zipentry) {
		ZipEntry nentry = new ZipEntry(zipentry.getName());
		long time = zipentry.getTime();
		if (time != -1) {
			nentry.setTime(time);
		}
		nentry.setMethod(zipentry.getMethod());
		return nentry;
	}

	private static void toPaths(Collection<Path> classpathurls, Collection<String> paths) throws InvalidPathException {
		if (paths == null) {
			return;
		}
		for (String cp : paths) {
			for (String url : cp.split(";+")) {
				if (url.isEmpty()) {
					continue;
				}
				classpathurls.add(Paths.get(url).toAbsolutePath().normalize());
			}
		}
	}

	private static class ClassBytes {
		protected final Path source;
		protected final byte[] bytes;

		protected boolean individualClassFile;
		protected ZipEntry zipEntry;

		public ClassBytes(Path source, byte[] bytes) {
			this.source = source;
			this.bytes = bytes;
		}
	}

	private static final class InputLoadingURLClassLoader extends URLClassLoader {
		private Map<String, byte[]> individualClassFiles = new TreeMap<>();

		private InputLoadingURLClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public void addClassFile(String classname, byte[] classbytes) {
			individualClassFiles.put(classname, classbytes);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				return super.findClass(name);
			} catch (ClassNotFoundException e) {
				byte[] cbytes = individualClassFiles.get(name);
				if (cbytes != null) {
					return defineClass(name, cbytes, 0, cbytes.length);
				}
				throw e;
			}
		}
	}

	private interface OutputHandler {
		public void handle(byte[] bytes);
	}

	private final class ZipOutputHandler implements OutputHandler {
		private final ZipEntry zipEntry;

		private ZipOutputHandler(ZipEntry zipentry) {
			this.zipEntry = zipentry;
		}

		@Override
		public void handle(byte[] bytes) {
			outputZipEntryBytes.put(zipEntry.getName(), new ZipEntryBytes(zipEntry, bytes));
		}
	}

	private final class OverwriteZipOutputHandler implements OutputHandler {
		private final Path zipPath;
		private final String zipEntryName;

		public OverwriteZipOutputHandler(Path zipPath, String zipEntryName) {
			this.zipPath = zipPath;
			this.zipEntryName = zipEntryName;
		}

		@Override
		public void handle(byte[] bytes) {
			//the file contents map should be already added to the overwrite map during setup 
			overwriteZipFileOutputBytes.get(zipPath).put(zipEntryName, bytes);
		}
	}

	private final class ClassFileOutputHandler implements OutputHandler {
		private final Path classFilePath;

		private ClassFileOutputHandler(Path p) {
			this.classFilePath = p;
		}

		@Override
		public void handle(byte[] bytes) {
			outputPathBytes.put(classFilePath, bytes);
		}
	}

	private static final class ZipEntryBytes {
		protected final ZipEntry zipEntry;
		protected final byte[] bytes;

		public ZipEntryBytes(ZipEntry zipEntry, byte[] bytes) {
			this.zipEntry = zipEntry;
			this.bytes = bytes;
		}
	}

	private static final class PrintStreamToolLogger implements ToolLogger {
		private final PrintStream outstream;

		private PrintStreamToolLogger(PrintStream outstream) {
			this.outstream = outstream;
		}

		@Override
		public void log(LogEntry entry) {
			outstream.println(entry.getMessage());
		}
	}

	private static final class OutputHandlerForwardingOutputConsumer implements OutputConsumer {
		public static final OutputHandlerForwardingOutputConsumer INSTANCE = new OutputHandlerForwardingOutputConsumer();

		@Override
		public void put(ToolInput<?> input, byte[] resultBytes) throws IOException {
			Object inputkey = input.getInputKey();
			if (inputkey instanceof OutputHandler) {
				((OutputHandler) inputkey).handle(resultBytes);
			}
		}
	}
}
