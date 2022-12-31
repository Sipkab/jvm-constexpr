package sipka.jvm.constexpr.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import sipka.jvm.constexpr.tool.log.AbstractSimpleToolLogger;
import sipka.jvm.constexpr.tool.log.LogEntry;
import sipka.jvm.constexpr.tool.options.DeconstructionDataAccessor;
import sipka.jvm.constexpr.tool.options.DeconstructionSelector;
import sipka.jvm.constexpr.tool.options.DeconstructorConfiguration;
import sipka.jvm.constexpr.tool.options.InlinerOptions;
import sipka.jvm.constexpr.tool.options.ToolInput;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.AnnotationVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassReader;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.ClassVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.FieldVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.MethodVisitor;
import sipka.jvm.constexpr.tool.thirdparty.org.objectweb.asm.Opcodes;
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

	protected Map<Class<?>, DeconstructorSettings> deconstructorSettings = new HashMap<>();

	public void call() throws Exception {
		if (overwrite && output != null) {
			throw new IllegalArgumentException(
					"Both -overwrite and -output were specified. Only one is allowed at a time.");
		}
		Path outputpath;
		if (!overwrite && output != null) {
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
		Collection<URL> classpathurls = new LinkedHashSet<>();
		Collection<URL> inputurls = new LinkedHashSet<>();
		toURLs(classpathurls, classpath);
		toURLs(inputurls, input);
		classloaderurls.addAll(classpathurls);
		for (URL inurl : inputurls) {
			if (!classloaderurls.add(inurl)) {
				throw new IllegalArgumentException("Duplicate URL in classpath and input: " + inurl);
			}
		}
		try (URLClassLoader cl = URLClassLoader.newInstance(classloaderurls.toArray(new URL[0]),
				getParentClassLoader())) {

			InlinerOptions options = new InlinerOptions();

			//scan the complete classpath for annotations
			for (URL url : classpathurls) {
				scanClasspath(options, url, cl, false);
			}
			for (URL url : inputurls) {
				scanClasspath(options, url, cl, true);
			}

			for (Entry<Class<?>, DeconstructorSettings> entry : deconstructorSettings.entrySet()) {
				DeconstructorSettings settings = entry.getValue();
				DeconstructionSelector selector = settings.toDeconstructionSelector();
				if (selector == null) {
					//not configured, only the settings instance is present
					continue;
				}
				options.getDeconstructorConfigurations().put(entry.getKey(), selector);
			}

			options.setLogger(new AbstractSimpleToolLogger() {
				@Override
				protected void log(LogEntry entry) {
					System.out.println(entry.getMessage());
				}
			});
			options.setOutputConsumer(new OutputConsumer() {
				@Override
				public void put(ToolInput<?> input, byte[] resultBytes) throws IOException {
					Object inputkey = input.getInputKey();
					if (inputkey instanceof OutputHandler) {
						((OutputHandler) inputkey).handle(resultBytes);
					}
				}
			});

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

	private static ClassLoader getParentClassLoader() {
		//available from JDK 9+
		try {
			return (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
		} catch (Exception e) {
			//okay, if we're running on Java 8
			return null;
		}
	}

	private void scanClasspath(InlinerOptions options, URL url, URLClassLoader cl, boolean input) throws Exception {
		if (!"file".equals(url.getProtocol())) {
			throw new IllegalArgumentException("Unsupported URL protocol: " + url);
		}

		Path path = Paths.get(url.toURI()).toAbsolutePath().normalize();

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
						AnalyzerClassVisitor analyzer;
						try {
							analyzer = analyzeClassFile(options, bytes, cl);
						} catch (Exception e) {
							throw new RuntimeException("Failed to analyze " + p, e);
						}
						if (input) {
							OutputHandler handler;
							if (overwrite) {
								handler = new ClassFileOutputHandler(p);
							} else {
								handler = getOutputHandler(analyzer, p, null);
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
				AnalyzerClassVisitor analyzer;
				try {
					analyzer = analyzeClassFile(options, bytes, cl);
				} catch (Exception e) {
					throw new RuntimeException("Failed to analyze " + path, e);
				}
				if (input) {
					if (outputZip) {
						ZipEntry ze = new ZipEntry(analyzer.classInternalName + ".class");
						ze.setLastModifiedTime(Files.getLastModifiedTime(path));
						outputZipEntryBytes.put(ze.getName(), new ZipEntryBytes(ze, bytes));
					}
					OutputHandler handler;
					if (overwrite) {
						handler = new ClassFileOutputHandler(path);
					} else {
						handler = getOutputHandler(analyzer, path, null);
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
							AnalyzerClassVisitor analyzer;
							try {
								analyzer = analyzeClassFile(options, bytes, cl);
							} catch (Exception e) {
								throw new RuntimeException("Failed to analyze " + ze.getName() + " in " + path, e);
							}
							OutputHandler handler;
							if (overwrite) {
								handler = new OverwriteZipOutputHandler(path, ze.getName());
							} else {
								handler = getOutputHandler(analyzer, null, ze);
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
	 * @param analyzer
	 * @param filepath
	 *            If the input is a file.
	 * @param zipentry
	 *            If the input is a zip entry.
	 * @return
	 * @throws IOException
	 */
	private OutputHandler getOutputHandler(AnalyzerClassVisitor analyzer, Path filepath, ZipEntry zipentry)
			throws IOException {
		if (outputDir) {
			return new ClassFileOutputHandler(outputDirectory.resolve(analyzer.classInternalName + ".class"));
		}
		if (outputZip) {
			ZipEntry ze;
			if (zipentry != null) {
				ze = cloneZipEntry(zipentry);
			} else {
				ze = new ZipEntry(analyzer.classInternalName + ".class");
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

	private interface OutputHandler {
		public void handle(byte[] bytes);
	}

	private AnalyzerClassVisitor analyzeClassFile(InlinerOptions options, byte[] bytes, URLClassLoader cl)
			throws Exception {
		ClassReader cr = new ClassReader(bytes);
		AnalyzerClassVisitor analyzer = new AnalyzerClassVisitor(ConstantExpressionInliner.ASM_API);
		cr.accept(analyzer, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
		if (!analyzer.isAnyConstantRelatedSettings()) {
			return analyzer;
		}
		Class<?> type = Class.forName(Type.getObjectType(analyzer.classInternalName).getClassName(), false, cl);
		if (analyzer.constantExpression) {
			options.getConstantTypes().add(type);
		}
		if (analyzer.enumType) {
			//add the fields as enum reconstructors
			for (Field field : type.getDeclaredFields()) {
				if (field.isEnumConstant()) {
					options.getConstantReconstructors().add(field);
				}
			}
		}
		for (NameDescriptor namedescriptor : analyzer.constantExpressionFields) {
			Field member = Utils.getFieldForDescriptor(type, namedescriptor.name, namedescriptor.descriptor);
			if (((member.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				options.getConstantFields().add(member);
			} else {
				options.getConstantReconstructors().add(member);
			}
		}
		for (NameDescriptor namedescriptor : analyzer.constantExpressionMethods) {
			Method member = Utils.getMethodForMethodDescriptor(type, analyzer.classInternalName,
					namedescriptor.descriptor, namedescriptor.name);
			options.getConstantReconstructors().add(member);
		}

		List<Field> equalityfields = new ArrayList<>(analyzer.deconstructorFields.size());
		for (NameDescriptor namedescriptor : analyzer.deconstructorFields) {
			Field field = Utils.getFieldForDescriptor(type, namedescriptor.name, namedescriptor.descriptor);
			if (field.isEnumConstant()) {
				//ignore, always used regardless of configuration
				//TODO maybe log it?
				continue;
			}
			if (!((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC)) {
				//TODO better logging
				System.out.println(
						"Non-static field market with @" + Deconstructor.class.getSimpleName() + " annotation: "
								+ Utils.memberDescriptorToPrettyString(Type.getType(namedescriptor.descriptor),
										Type.getObjectType(analyzer.classInternalName), namedescriptor.name));
				continue;
			}
			equalityfields.add(field);
			deconstructorSettings.computeIfAbsent(field.getType(), x -> new DeconstructorSettings()).addField(field);
		}

		if (analyzer.deconstructorMethods.size() > 1) {
			throw new IllegalArgumentException(
					"Multiple method deconstructors specified for: " + analyzer.classInternalName);
		}
		//TODO support multiple executable deconstructors
		for (NameDescriptor namedescriptor : analyzer.deconstructorMethods) {
			Executable e = Utils.getExecutableForDescriptor(type, analyzer.classInternalName, namedescriptor.name,
					namedescriptor.descriptor);
			java.lang.reflect.Parameter[] params = e.getParameters();
			DeconstructionDataAccessor[] parameterdataaccessors = new DeconstructionDataAccessor[params.length];
			for (int i = 0; i < parameterdataaccessors.length; i++) {
				java.lang.reflect.Parameter param = params[i];
				Class<?> paramtype = param.getType();
				String paramname = param.getName();
				//search getter for paramname, that has a return type of paramtype
				Collection<? extends Method> getters = searchGetter(type, paramtype, paramname);
				if (getters.isEmpty()) {
					throw new IllegalArgumentException("Getter not found for deconstructor: " + e + " with parameter["
							+ i + "] " + paramtype + " " + paramname);
				}
				if (getters.size() != 1) {
					throw new IllegalArgumentException("Multiple getters found for deconstructor: " + e
							+ " with parameter[" + i + "] " + paramtype + " " + paramname + ": " + getters);
				}
				Method gettermethod = getters.iterator().next();
				parameterdataaccessors[i] = DeconstructionDataAccessor.createForMethod(gettermethod);
			}
			deconstructorSettings
					.computeIfAbsent(Utils.getExecutableEffectiveReturnType(e), x -> new DeconstructorSettings())
					.addExecutable(DeconstructorConfiguration.createExecutable(e, parameterdataaccessors));
		}
		return analyzer;
	}

	private static Collection<? extends Method> searchGetter(Class<?> type, Class<?> paramtype, String paramname) {
		Map<NameDescriptor, Method> methods = new TreeMap<>();
		searchGetterImpl(type, paramtype, paramname.toLowerCase(Locale.ROOT), methods);
		return methods.values();
	}

	private static void searchGetterImpl(Class<?> type, Class<?> paramtype, String paramname,
			Map<NameDescriptor, ? super Method> methods) {
		if (type == null) {
			return;
		}
		for (Method m : type.getDeclaredMethods()) {
			if (m.getReturnType() != paramtype || m.getParameterCount() != 0) {
				continue;
			}
			String methodnamelowercase = m.getName().toLowerCase(Locale.ROOT);
			if (!methodnamelowercase.contains(paramname)) {
				continue;
			}
			if (methodnamelowercase.equals(paramname) || methodnamelowercase.equals("get" + paramname)) {
				methods.putIfAbsent(new NameDescriptor(m.getName(), Type.getMethodDescriptor(m)), m);
			}
		}
		searchGetterImpl(type.getSuperclass(), paramtype, paramname, methods);
		for (Class<?> itf : type.getInterfaces()) {
			searchGetterImpl(itf, paramtype, paramname, methods);
		}
	}

	private static void toURLs(Collection<URL> classpathurls, Collection<String> paths)
			throws InvalidPathException, MalformedURLException {
		if (paths == null) {
			return;
		}
		for (String cp : paths) {
			for (String url : cp.split(";+")) {
				if (url.isEmpty()) {
					continue;
				}
				classpathurls.add(Paths.get(url).toUri().toURL());
			}
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

	private static final class DeconstructorSettings {

		private Collection<Field> equalityFields = new LinkedHashSet<>();
		private Collection<DeconstructorConfiguration> executableConfigurations = new LinkedHashSet<>();

		public DeconstructionSelector toDeconstructionSelector() {
			if (executableConfigurations.size() > 1) {
				throw new IllegalArgumentException("Too many executable deconstructors: " + executableConfigurations);
			}
			DeconstructionSelector execdecons;
			if (!executableConfigurations.isEmpty()) {
				execdecons = DeconstructionSelector.getForConfiguration(executableConfigurations.iterator().next());
			} else {
				execdecons = null;
			}
			return DeconstructionSelector.getMultiSelector(
					DeconstructionSelector.getStaticFieldEquality(equalityFields.toArray(new Field[0])), execdecons);
		}

		public void addField(Field field) {
			equalityFields.add(field);

		}

		public void addExecutable(DeconstructorConfiguration execconfig) {
			executableConfigurations.add(execconfig);
		}
	}

	private static final class AnalyzerClassVisitor extends ClassVisitor {
		protected static final String CONSTANTEXPRESSION_DESCRIPTOR = Type.getDescriptor(ConstantExpression.class);
		protected static final String DECONSTRUCTOR_DESCRIPTOR = Type.getDescriptor(Deconstructor.class);

		protected String classInternalName;
		protected boolean enumType;
		protected boolean constantExpression;

		protected Set<NameDescriptor> constantExpressionMethods = new TreeSet<>();
		protected Set<NameDescriptor> constantExpressionFields = new TreeSet<>();
		protected Set<NameDescriptor> deconstructorMethods = new TreeSet<>();
		protected Set<NameDescriptor> deconstructorFields = new TreeSet<>();

		private AnalyzerClassVisitor(int api) {
			super(api);
		}

		public boolean isAnyConstantRelatedSettings() {
			return enumType || constantExpression || !constantExpressionMethods.isEmpty()
					|| !constantExpressionFields.isEmpty() || !deconstructorMethods.isEmpty()
					|| !deconstructorFields.isEmpty();
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			classInternalName = name;
			enumType = ((access & Opcodes.ACC_ENUM) == Opcodes.ACC_ENUM);
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			if (CONSTANTEXPRESSION_DESCRIPTOR.equals(descriptor)) {
				constantExpression = true;
			}
			return super.visitAnnotation(descriptor, visible);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
				@Override
				public AnnotationVisitor visitAnnotation(String annotdescriptor, boolean visible) {
					if (CONSTANTEXPRESSION_DESCRIPTOR.equals(annotdescriptor)) {
						constantExpressionMethods.add(new NameDescriptor(name, descriptor));
					}
					if (DECONSTRUCTOR_DESCRIPTOR.equals(annotdescriptor)) {
						deconstructorMethods.add(new NameDescriptor(name, descriptor));
					}
					return super.visitAnnotation(annotdescriptor, visible);
				}
			};
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			return new FieldVisitor(api, super.visitField(access, name, descriptor, signature, value)) {
				@Override
				public AnnotationVisitor visitAnnotation(String annotdescriptor, boolean visible) {
					if (CONSTANTEXPRESSION_DESCRIPTOR.equals(annotdescriptor)) {
						constantExpressionFields.add(new NameDescriptor(name, descriptor));
					}
					if (DECONSTRUCTOR_DESCRIPTOR.equals(annotdescriptor)) {
						deconstructorFields.add(new NameDescriptor(name, descriptor));
					}
					return super.visitAnnotation(annotdescriptor, visible);
				}
			};
		}
	}

}
