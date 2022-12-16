package org.kiwiproject.beta.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.google.common.annotations.Beta;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When placed on a method or constructor, indicates that the member is called via
 * reflection, and is this used and should not be considered for removal. When placed
 * on a field, indicates that the field is accesssed via reflection to get or set it.
 * <p>
 * When this annotation is present, you might also consider adding {@code @SuppressWarnings("unused")}.
 * This is necessary since {@link SuppressWarnings} is not {@link Inherited} so we cannot
 * include it in this annotation.
 */
@Documented
@Target({METHOD, CONSTRUCTOR, FIELD})
@Retention(RetentionPolicy.SOURCE)
@Beta
public @interface AccessedViaReflection {

    /**
     * A description of when, where, why, how this annotated element is accessed via reflection.
     */
    String value();
}