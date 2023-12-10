package org.kiwiproject.beta.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

import com.google.common.annotations.Beta;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When placed on a method or constructor, indicates that the member is called via
 * reflection, and is this used and should not be considered for removal. When placed
 * on a field, it indicates that the field is accessed via reflection to get or set it.
 * When placed on a type (class, interface, annotation, enum, or record), indicates that
 * the type may be loaded via reflection using {@link Class#forName(String)} or a similar
 * mechanism. Placing on a type can also mean that the type and/or some or all of its
 * members may be accessed reflectively.
 * <p>
 * When this annotation is present, you might also consider adding {@code @SuppressWarnings("unused")}.
 * This is necessary since {@link SuppressWarnings} is not {@link Inherited} so we cannot
 * include it in this annotation.
 */
@Documented
@Target({METHOD, CONSTRUCTOR, FIELD, TYPE})
@Retention(RetentionPolicy.SOURCE)
@Beta
public @interface AccessedViaReflection {

    /**
     * Optional description of when, where, why, how this annotated element is accessed via reflection.
     * <p>
     * While this value is optional, it is recommended, since it can be difficult to track down when
     * and where reflective code invokes methods, constructors, etc.
     */
    String value() default "";
}
