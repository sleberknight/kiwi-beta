package org.kiwiproject.beta.util.function;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.google.common.annotations.Beta;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.function.Predicate;

/**
 * A {@link Predicate} that wraps a Predicate and tracks the number of
 * true and false results.
 * <p>
 * This should be used only on one {@link java.util.stream.Stream Stream}, unless you want to
 * continue to accumulate the number of true and false results
 * over multiple streams.
 * <p>
 * Exceptions thrown by the wrapped Predicate are passed through.
 */
@Beta
public class CountingPredicate<T> implements Predicate<T> {

    private final Predicate<T> predicate;
    private long trueCount;
    private long falseCount;

    /**
     * Create a new instance that wraps {@code predicate} in order to
     * count the number of true and false results.
     *
     * @param predicate the original {@link Predicate} to wrap
     */
    public CountingPredicate(Predicate<T> predicate) {
        this.predicate = requireNotNull(predicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(T t) {
        var result = predicate.test(t);
        if (result) {
            ++trueCount;
        } else {
            ++falseCount;
        }
        return result;
    }

    /**
     * Return the number of times this predicate has gotten a {@code true} result.
     *
     * @return the total number of true results
     */
    @NonNegative
    public long trueCount() {
        return trueCount;
    }

    /**
     * Return the number of times this predicate has gotten a {@code false} result.
     *
     * @return the total number of false results
     */
    @NonNegative
    public long falseCount() {
        return falseCount;
    }
}
