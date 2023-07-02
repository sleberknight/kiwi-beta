package org.kiwiproject.beta.test.junit.jupiter.params.provider;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.beta.base.KiwiStrings2.randomCaseVariants;
import static org.kiwiproject.beta.base.KiwiStrings2.standardCaseVariants;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.Set;
import java.util.stream.Stream;

/**
 * An {@link ArgumentsProvider} that provides random capitalization of an input value.
 *
 * @see RandomCapitalizationSource
 */
public class RandomCapitalizationArgumentsProvider
        implements ArgumentsProvider, AnnotationConsumer<RandomCapitalizationSource> {

    private RandomCapitalizationSource randomCapitalizationSource;

    @Override
    public void accept(RandomCapitalizationSource randomCapitalizationSource) {
        this.randomCapitalizationSource = randomCapitalizationSource;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        var value = randomCapitalizationSource.value();
        var count = randomCapitalizationSource.count();

        checkArgumentNotBlank(value, "value must not be blank");
        checkArgument(count > 0, "count must be greater than zero");

        Set<String> variants = getCaseVariants(value, count, randomCapitalizationSource.type());
        return variants.stream().map(Arguments::of);
    }

    private static Set<String> getCaseVariants(String value, int count, RandomCapitalizationSource.Type type) {
        if (type == RandomCapitalizationSource.Type.RANDOM) {
            return randomCaseVariants(value, count);
        }
        return standardCaseVariants(value);
    }
}
