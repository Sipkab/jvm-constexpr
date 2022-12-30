package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import sipka.jvm.constexpr.tool.ConstantExpressionInliner;
import sipka.jvm.constexpr.tool.OutputConsumer;
import sipka.jvm.constexpr.tool.log.ToolLogger;

/**
 * Options for the constant inliner tool.
 * <p>
 * Use with {@link ConstantExpressionInliner#run(InlinerOptions)}.
 */
public final class InlinerOptions {
	protected Collection<ToolInput<?>> inputs = new ArrayList<>();
	protected OutputConsumer outputConsumer;

	protected Collection<Field> constantFields = new HashSet<>();

	protected Collection<Class<?>> constantTypes = new HashSet<>();

	protected Map<Class<?>, DeconstructionSelector> deconstructorConfigurations = new HashMap<>();

	protected Collection<Member> constantReconstructors = new ArrayList<>();

	protected ToolLogger logger;

	/**
	 * Creates an empty instance.
	 */
	public InlinerOptions() {
	}

	/**
	 * Sets the inputs for the inliner.
	 * <p>
	 * The inliner will read and process only these specified inputs. Classes that are referenced by and of the inputs
	 * are not processed automatically.
	 * 
	 * @param inputs
	 *            The inputs.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setInputs(Collection<ToolInput<?>> inputs) throws NullPointerException {
		Objects.requireNonNull(inputs, "inputs");
		this.inputs = inputs;
	}

	/**
	 * Gets the inputs.
	 * <p>
	 * Modifications to the returned collection may or may not be propagated back to the backing collection.
	 * 
	 * @return The inputs.
	 * @see #getInputs()
	 */
	public Collection<ToolInput<?>> getInputs() {
		return inputs;
	}

	/**
	 * Sets the output consumer for the inliner tool.
	 * <p>
	 * The given output consumer will be called for each processed class that have been modified during the inlining
	 * process. If a class wasn't modified, the output consumer may not get called.
	 * 
	 * @param outputConsumer
	 *            The output consumer.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setOutputConsumer(OutputConsumer outputConsumer) throws NullPointerException {
		Objects.requireNonNull(outputConsumer, "outputConsumer");
		this.outputConsumer = outputConsumer;
	}

	/**
	 * Gets the output consumer.
	 * 
	 * @return The output consumer or <code>null</code> if not yet set.
	 * @see #setOutputConsumer(OutputConsumer)
	 */
	public OutputConsumer getOutputConsumer() {
		return outputConsumer;
	}

	/**
	 * Sets the constant fields.
	 * <p>
	 * The constant fields are handled in the following way:
	 * <ul>
	 * <li>They're inlined by the tool. Their initializer is executed, and the resulting value is inlined in the class
	 * file, and possibly to other references to this field.</li>
	 * <li>The contents of the fields are considered to be immutable, they hold no mutable state. They won't get
	 * modified by any of the code that uses them (even if they have an array type). They can be eagerly inlined.</li>
	 * </ul>
	 * <p>
	 * The fields specified here should be <code>static final</code> fields.
	 * 
	 * @param constantFields
	 *            The constant fields.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setConstantFields(Collection<Field> constantFields) throws NullPointerException {
		Objects.requireNonNull(constantFields, "constantFields");
		this.constantFields = constantFields;
	}

	/**
	 * Gets the constant fields.
	 * <p>
	 * Modifications to the returned collection may or may not be propagated back to the backing collection.
	 * 
	 * @return The constant fields.
	 * @see #setConstantFields(Collection)
	 */
	public Collection<Field> getConstantFields() {
		return constantFields;
	}

	/**
	 * Sets the constant types.
	 * <p>
	 * Constant types are ones that:
	 * <ul>
	 * <li>Immutable, they hold no mutable state.</li>
	 * <li>All of their non-static functions are pure, and don't access the properties of the executing environment.
	 * (Like Locale, other resources, files, etc...)</li>
	 * <li>Their constructors can be used for constant optimization.</li>
	 * </ul>
	 * <p>
	 * Note however, the {@link Object#hashCode()} function is not automatically used for constant optimization for
	 * constant types, as its result may not be stable. If the <code>hashCode</code> is known to be stable, add it as a
	 * {@linkplain #setConstantReconstructors(Collection) reconstructor function}.
	 * <p>
	 * Example for constant types: {@link Integer}, {@link UUID}, {@link LocalDate}, {@link Month}, etc...
	 * <p>
	 * As an example, note that {@link String} is not a constant type, because some of its methods may access the
	 * default {@link Locale}, and those methods cannot be inlined, because they depend on the executing environment.
	 * 
	 * @param constantTypes
	 *            The constant types.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setConstantTypes(Collection<Class<?>> constantTypes) throws NullPointerException {
		Objects.requireNonNull(constantTypes, "constantTypes");
		this.constantTypes = constantTypes;
	}

	/**
	 * Gets the constant types.
	 * <p>
	 * Modifications to the returned collection may or may not be propagated back to the backing collection.
	 * 
	 * @return The contant types.
	 * @see #setConstantTypes(Collection)
	 */
	public Collection<Class<?>> getConstantTypes() {
		return constantTypes;
	}

	/**
	 * Sets the constant deconstructors.
	 * <p>
	 * The deconstructors specify the way of serializing a constant value back to the JVM stack. The configuration
	 * holder for this is the {@link DeconstructorConfiguration} class, which can be instantiated using its static
	 * factory methods for an appropriate use-case.
	 * <p>
	 * In general, deconstructors are a way of reconstructing the optimized constant instance during runtime.
	 * <p>
	 * An example of a deconstructor:
	 * 
	 * <pre>
	 * class MyClass {
	 * 	private final int value;
	 * 
	 * 	MyClass(int value) {
	 * 		this.value = value;
	 * 	}
	 * 
	 * 	public int getValue() {
	 * 		return value;
	 * 	}
	 * }
	 * </pre>
	 * 
	 * In the above case, the constructor with the <code>value</code> argument can be specified as a deconstructor. In
	 * this case, if a <code>MyClass</code> constant is processed by the tool, then it will be written back to the stack
	 * in the following way:
	 * 
	 * <pre>
	 * new MyClass               # create a new MyClass instance
	 * dup                       # duplicate the new MyClass reference on the stack
	 * ldc MyClass.getValue()    # the getValue() function is invoked, 
	 *                           # and the result is written to the stack as an ldc instruction
	 * invokespecial MyClass.MyClass(int)    # invoke the constructor
	 * </pre>
	 * 
	 * Each class can have only a single deconstructor, which is called when an object with that exact type is written
	 * back to the JVM stack.
	 * <p>
	 * Classes without deconstructors can't be optimized, therefore it is recommended that they are set for classes that
	 * are relevant for constant optimization.
	 * 
	 * @param deconstructorConfigurations
	 *            The deconstructor configurations.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see DeconstructionSelector
	 * @see DeconstructorConfiguration
	 */
	public void setDeconstructorConfigurations(Map<Class<?>, DeconstructionSelector> deconstructorConfigurations)
			throws NullPointerException {
		Objects.requireNonNull(deconstructorConfigurations, "deconstructorConfigurations");
		this.deconstructorConfigurations = deconstructorConfigurations;
	}

	/**
	 * Gets the deconstructor configurations.
	 * <p>
	 * Modifications to the returned collection may or may not be propagated back to the backing collection.
	 * 
	 * @return The deconstructor configurations.
	 * @see #setDeconstructorConfigurations(Map)
	 */
	public Map<Class<?>, DeconstructionSelector> getDeconstructorConfigurations() {
		return deconstructorConfigurations;
	}

	/**
	 * Sets the constant reconstructor fields, methods and constructors.
	 * <p>
	 * Constant reconstructors are class members that can be invoked/accessed during constant optimization to get the
	 * value that they designate.
	 * <p>
	 * The tool will examine the code of the processed classes. If it encounters an instruction that corresponds to one
	 * of the constant reconstructors, then it will attempt to invoke/access this reconstructor to retrieve the constant
	 * value that they compute.
	 * <p>
	 * After this, the inliner tool will search for a matching {@linkplain #setDeconstructorConfigurations(Map)
	 * deconstructor configuration} and attempt to write back the computed value to the stack.
	 * <p>
	 * Basically, the constant reconstructors set here are the settings for which fields, methods, and constructors can
	 * be processed as part of the inlining.
	 * <p>
	 * An example, let's say that the {@link Integer#valueOf(String, int)} method is set as a constant reconstructor. If
	 * the inliner tool sees the following instructions on the stack:
	 * 
	 * <pre>
	 * ldc "890A"
	 * ldc 16
	 * invokestatic Integer.valueOf(String, int)
	 * </pre>
	 * 
	 * Then it will call this method, and get an Integer(123) instance. (This is when the tool <em>reconstructs</em> the
	 * constant from the stack.) It will then use the deconstructor for {@link Integer} to write it back to the stack as
	 * follows:
	 * 
	 * <pre>
	 * ldc 35082
	 * invokestatic Integer.valueOf(int)
	 * </pre>
	 * 
	 * As a result, the hexadecimal parsing of <code>890A</code> has been performed by the tool, and a more efficient
	 * instantiation of {@link Integer} is used instead.
	 * 
	 * @param constantReconstructors
	 *            The constant reconstructors. The collections should only contain {@link Constructor}, {@link Method}
	 *            or {@link Field} instances.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public void setConstantReconstructors(Collection<Member> constantReconstructors) throws NullPointerException {
		Objects.requireNonNull(constantReconstructors, "constantReconstructors");
		this.constantReconstructors = constantReconstructors;
	}

	/**
	 * Gets the constant reconstructors.
	 * <p>
	 * Modifications to the returned collection may or may not be propagated back to the backing collection.
	 * 
	 * @return The constant reconstructors.
	 * @see #setConstantReconstructors(Collection)
	 */
	public Collection<Member> getConstantReconstructors() {
		return constantReconstructors;
	}

	/**
	 * Sets the logger for the tool invocation.
	 * <p>
	 * The given logger will be called as the tool is processing the inputs.
	 * 
	 * @param logger
	 *            The logger.
	 */
	public void setLogger(ToolLogger logger) {
		this.logger = logger;
	}

	/**
	 * Gets the logger.
	 * 
	 * @return The logger, may be <code>null</code> if not set.
	 * @see #setLogger(ToolLogger)
	 */
	public ToolLogger getLogger() {
		return logger;
	}

}
