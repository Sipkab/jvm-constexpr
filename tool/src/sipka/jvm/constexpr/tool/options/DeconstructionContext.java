package sipka.jvm.constexpr.tool.options;

import sipka.jvm.constexpr.tool.log.ConfigClassMemberInaccessibleLogEntry;

/**
 * A context interface when selecting a {@linkplain DeconstructorConfiguration deconstruction configuraiton}.
 * <p>
 * Used in {@link DeconstructionSelector#chooseDeconstructorConfiguration(DeconstructionContext, Object)}.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface DeconstructionContext {
	/**
	 * Gets the currently optimized method where the deconstruction is taking place.
	 * 
	 * @return The method member reference.
	 */
	public MemberReference getOptimizedMethod();

	/**
	 * Reports a log entry about an issue where a class or one of its member wasn't accessible for some reason.
	 * <p>
	 * A log entry for a given member will be only logged once, regardless of the nature of the exception.
	 * 
	 * @param classInternalName
	 *            The internal name of the class.
	 * @param memberName
	 *            The name of the member.
	 * @param memberDescriptor
	 *            The descriptor of the member.
	 * @param exception
	 *            The cause exception.
	 * @see ConfigClassMemberInaccessibleLogEntry
	 */
	public void logConfigClassMemberInaccessible(String classInternalName, String memberName, String memberDescriptor,
			Throwable exception);
}
