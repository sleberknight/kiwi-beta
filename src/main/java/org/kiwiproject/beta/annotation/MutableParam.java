package org.kiwiproject.beta.annotation;

import com.google.common.annotations.Beta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to make it clear that a method/constructor parameter might be mutated, e.g. auditing
 * properties such as createdAt, createdBy, etc. that are automatically set on a persistent entity.
 * <p>
 * While we consider mutating arguments in any way to be generally poor practice that can lead to
 * unexpected and/or confusing behavior, sometimes it would be impossible or much more difficult to
 * avoid doing than to just make the behavior explicit. Consider, for example, the Java Collections
 * shuffle method which shuffles a collection in-place. It might be better to create a new shuffled
 * Collection rather than change in-place, but in some situations like unit tests it may be
 * acceptable.
 * <p>
 * At the very least, adding this annotation makes it clear that a parameter might be changed in
 * some way. And, it can also illustrate poor design if this annotation is "all over the place"
 * in a code base. Mutating constructor and method parameters should be the exception, not the rule.
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
@Beta
public @interface MutableParam {

    /**
     * Optional description about the possible parameter mutation.
     */
    String value() default "";
}
