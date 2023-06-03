package sipka.jvm.constexpr.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * Marks the element to be used as a constant deconstructor.
 * <p>
 * Constant deconstructors specify the way of serializing a constant value back to the JVM stack. Elements annotated
 * with <code>@Deconstructor</code> are used to determine how the optimized value should be written back to the JVM
 * stack as bytecode instructions.
 * <p>
 * The following elements can be annotated:
 * <ul>
 * <li>{@link ElementType#CONSTRUCTOR CONSTRUCTOR}: Constructors in which case the object will be deconstructed back as
 * a new instance creation. E.g. {@link UUID#UUID(long, long)}</li>
 * <li>{@link ElementType#METHOD METHOD}: Static methods can also be used to deconstruct an object. The method will be
 * used for deconstructing object with the same type as the declared return type.</li>
 * <li>{@link ElementType#FIELD FIELD}: Annotated static fields are used to compare the object using equality, and a
 * reference to this field is used if the equality matched.</li>
 * </ul>
 * The parameters for constructors and methods are determined using the parameter names of the methods. The inliner tool
 * will look for a method based on the parameter name. It will search for a no-arg method that has the same return type
 * as the parameter type. The method name should lowercase-match the lowercase parameter name, or the parameter name
 * prepended with get.
 * <p>
 * E.g. For a parameter CharSequence myMessage:
 * <ul>
 * <li><code>CharSequence myMessage()</code>: matches</li>
 * <li><code>Object myMessage()</code>: doesn't match</li>
 * <li><code>String myMessage()</code>: doesn't match</li>
 * <li><code>CharSequence mymessage()</code>: matches</li>
 * <li><code>CharSequence getmymessage()</code>: matches</li>
 * <li><code>CharSequence getMyMessage()</code>: matches</li>
 * <li><code>CharSequence GETMYMESSAGE()</code>: matches</li>
 * <li><code>CharSequence getMyMessage(int index)</code>: doesn't match</li>
 * </ul>
 * The annotation is roughly equivalent to the
 * {@link sipka.jvm.constexpr.tool.options.InlinerOptions#setDeconstructorConfigurations(java.util.Map)
 * InlinerOptions.setDeconstructorConfigurations} configuration method.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD })
public @interface Deconstructor {

}
