package sipka.jvm.constexpr.tool.options;

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
	public DeconstructorConfiguration chooseDeconstructorConfiguration(Object value);

	public static DeconstructionSelector create(DeconstructorConfiguration config) {
		return val -> config;
	}
}
