package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
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

	public void setDeconstructorConfigurations(Map<Class<?>, DeconstructionSelector> deconstructorConfigurations)
			throws NullPointerException {
		Objects.requireNonNull(deconstructorConfigurations, "deconstructorConfigurations");
		this.deconstructorConfigurations = deconstructorConfigurations;
	}

	public Map<Class<?>, DeconstructionSelector> getDeconstructorConfigurations() {
		return deconstructorConfigurations;
	}

	public void setConstantReconstructors(Collection<Member> constantReconstructors) throws NullPointerException {
		Objects.requireNonNull(constantReconstructors, "constantReconstructors");
		this.constantReconstructors = constantReconstructors;
	}

	public Collection<Member> getConstantReconstructors() {
		return constantReconstructors;
	}

}
