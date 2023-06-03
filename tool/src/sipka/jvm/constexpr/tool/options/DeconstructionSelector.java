package sipka.jvm.constexpr.tool.options;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Chooses how an object should be deconstructed back to the method stack.
 * <p>
 * Implementations may choose different {@link DeconstructorConfiguration DeconstructorConfigurations} based on the
 * contents of the deconstructed object.
 * <p>
 * Clients may implement this interface.
 * <p>
 * Use {@link #getForConfiguration(DeconstructorConfiguration)} for a simple instance that always uses the given config.
 */
public interface DeconstructionSelector {
	/**
	 * Chooses the actual deconstructor configuration for the given value.
	 * 
	 * @param deconstructioncontext
	 *            The context of the current deconstruction.
	 * @param value
	 *            The constant value. Never <code>null</code>
	 * @return The deconstructor configuration. May be <code>null</code> if the value cannot be deconstructed.
	 */
	public DeconstructorConfiguration chooseDeconstructorConfiguration(DeconstructionContext deconstructioncontext,
			Object value);

	/**
	 * Creates a new instance that always returns the argument configuration regardless of the value.
	 * 
	 * @param config
	 *            The configuration.
	 * @return The selector instance.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static DeconstructionSelector getForConfiguration(DeconstructorConfiguration config)
			throws NullPointerException {
		return new SimpleDeconstructionSelector(config);
	}

	public static DeconstructionSelector getStaticFieldEquality(Field... fields) throws NullPointerException {
		Objects.requireNonNull(fields, "fields");
		if (fields.length == 0) {
			return null;
		}
		//defensive copy
		fields = fields.clone();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i] == null) {
				throw new NullPointerException("fields[" + i + "]");
			}
		}
		return new StaticFieldEqualityDeconstructionSelector(fields);
	}

	public static DeconstructionSelector getMultiSelector(DeconstructionSelector... delegates)
			throws NullPointerException {
		if (delegates.length == 0) {
			return null;
		}
		List<DeconstructionSelector> createdelegates = new ArrayList<>();
		for (int i = 0; i < delegates.length; i++) {
			DeconstructionSelector selector = delegates[i];
			if (selector == null) {
				continue;
			}
			if (selector instanceof MultiDeconstructionSelector) {
				createdelegates.addAll(((MultiDeconstructionSelector) selector).getDelegates());
			} else {
				createdelegates.add(selector);
			}
		}
		if (createdelegates.isEmpty()) {
			return null;
		}
		if (createdelegates.size() == 1) {
			return createdelegates.get(0);
		}
		return new MultiDeconstructionSelector(createdelegates);
	}
}
