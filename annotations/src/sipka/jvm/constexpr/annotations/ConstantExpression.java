package sipka.jvm.constexpr.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Marks the element as a constant, making it subject to optimization.
 * <p>
 * In general, marking an element as constant expression makes it available for the optimizer to use when working with
 * objects. A marked element will be usable to recreate objects, or access its data.
 * <p>
 * The optimization behaviour depends on the type of the annotated element:
 * <ul>
 * <li>{@link ElementType#TYPE TYPE}: The type will be considered to be a constant type. It is configured for the
 * optimizer via {@link sipka.jvm.constexpr.tool.options.InlinerOptions#setConstantTypes(Collection)
 * InlinerOptions.setConstantTypes}. A constant type is considered to make all of its non-static fields, constructors
 * and non-static methods (except {@link Object#hashCode()}) constant expressions. <br>
 * The optimizer is allowed to access all of the methods and fields of a constant type. The {@link Object#hashCode()} is
 * not automatically used, even if a type is annotated. If the hash code of the type is stable, you can annotate that as
 * well to make it usable.</li>
 * <li>{@link ElementType#FIELD FIELD}: An annotated non-static field will be accessable by the optimizer. <br>
 * If a <code>static final</code> field is annotated, its initializer will be forcibly run by the optimizer, and the
 * resulting value will be inlined. You can use the annotation on a <code>static final</code> field to always compute
 * the value of it, even if it's not an actual constant, like {@link System#currentTimeMillis()}. See
 * {@link sipka.jvm.constexpr.tool.options.InlinerOptions#setConstantFields(Collection)
 * InlinerOptions.setConstantFields}.</li>
 * <li>{@link ElementType#METHOD METHOD}: An annotated non-static method will be usable by the optimizer. <br>
 * If a <code>static</code> method is annotated, that makes the method callable when evaluating constant values.</li>
 * <li>{@link ElementType#CONSTRUCTOR CONSTRUCTOR}: An annotated constructor will be usable by the optimizer to
 * reconstruct the constant objects.</li>
 * </ul>
 * <p>
 * Based on the above, it should hold true that annotating a {@link ElementType#TYPE TYPE} is the same as individually
 * annotating all of its non-static fields, constructors, and non-static methods except {@link Object#hashCode()}.
 * <p>
 * The annotation is roughly equivalent to the
 * {@link sipka.jvm.constexpr.tool.options.InlinerOptions#setConstantReconstructors(Collection)
 * InlinerOptions.setConstantReconstructors} settings, with the addition that it is also used to force inline
 * <code>static final</code> fields.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
public @interface ConstantExpression {

}
