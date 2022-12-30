package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;

/**
 * Chooses how an object should be deconstructed back to the method stack.
 * <p>
 * Implementations may choose different {@link DeconstructorConfiguration DeconstructorConfigurations} based on the
 * contents of the deconstructed object.
 * <p>
 * Clients may implement this interface.
 * <p>
 * Use {@link #create(DeconstructorConfiguration)} for a simple instance that always uses the given config.
 */
public interface DeconstructionSelector {
	/**
	 * Chooses the actual deconstructor configuration for the given value.
	 * 
	 * @param value
	 *            The constant value. Never <code>null</code>
	 * @return The deconstructor configuration. May be <code>null</code> if the value cannot be deconstructed.
	 */
	public DeconstructorConfiguration chooseDeconstructorConfiguration(Object value);

	/**
	 * Creates a new instance that always returns the argument configuration regardless of the value.
	 * 
	 * @param config
	 *            The configuration.
	 * @return The selector instance.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static DeconstructionSelector create(DeconstructorConfiguration config) throws NullPointerException {
		return new SimpleDeconstructionSelector(config);
	}

	public static DeconstructionSelector createStaticFieldEquality(Field[] fields, DeconstructionSelector delegate)
			throws NullPointerException {
		if (fields == null || fields.length == 0) {
			return delegate;
		}
		return new StaticFieldEqualityDeconstructionSelector(delegate, fields);
	}
}
