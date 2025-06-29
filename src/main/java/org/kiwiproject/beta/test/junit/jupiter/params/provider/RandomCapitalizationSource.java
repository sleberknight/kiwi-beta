package org.kiwiproject.beta.test.junit.jupiter.params.provider;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link RandomCapitalizationSource} is an {@link ArgumentsSource} that provides values having random capitalization
 * of a given input for use in parameterized tests.
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(RandomCapitalizationArgumentsProvider.class)
public @interface RandomCapitalizationSource {

    /**
     * The value which will be randomly capitalized.
     *
     * @return the input value
     */
    String value();

    /**
     * The desired number of randomly capitalized values to provide.
     * <p>
     * The actual number provided may be lower than this value if it is more than 2^N, where N is the length of
     * the input value. See {@link org.kiwiproject.beta.base.KiwiStrings2#randomCaseVariants(String, int)} for
     * details on this mathematical limit.
     *
     * @return the number of desired values
     */
    int count() default 3;

    /**
     * The type of capitalization to provide.
     */
    enum Type {

        /**
         * Provides randomly capitalized values.
         */
        RANDOM,

        /**
         * Provides three "standard" case variants.
         *
         * @see org.kiwiproject.beta.base.KiwiStrings2#standardCaseVariants(String)
         */
        STANDARD
    }

    /**
     * The type of capitalized values to provide.
     * <p>
     * Note that when {@link Type#STANDARD} is chosen, the maximum number of values is three.
     *
     * @return the type
     * @see org.kiwiproject.beta.base.KiwiStrings2#standardCaseVariants(String)
     */
    Type type() default Type.RANDOM;
}
