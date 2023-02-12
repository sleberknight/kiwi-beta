package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("KiwiRunnables")
class KiwiRunnablesTest {

    @Nested
    class RunAllQuietly {

        @Test
        void shouldCallRunnables() {
            var called1 = new AtomicBoolean();
            var called2 = new AtomicBoolean();
            var called3 = new AtomicBoolean();

            KiwiRunnables.runAllQuietly(
                    () -> called1.set(true),
                    () -> called2.set(true),
                    () -> called3.set(true));

                    assertThat(called1).isTrue();
            assertThat(called2).isTrue();
            assertThat(called3).isTrue();
        }

        @Test
        void shouldSuppressExceptions() {
            var called1 = new AtomicBoolean();
            var called2 = new AtomicBoolean();
            var called3 = new AtomicBoolean();

            KiwiRunnables.runAllQuietly(
                    () -> {
                        called1.set(true);
                        throw new RuntimeException("I failed");
                    },
                    () -> called2.set(true),
                    () -> {
                        called3.set(true);
                        throw new IOException("I/O failed");
                    });

                    assertThat(called1).isTrue();
            assertThat(called2).isTrue();
            assertThat(called3).isTrue();
        }
    }

    @Nested
    class RunQuietly {

        @Test
        void shouldCallRunnable() {
            var called = new AtomicBoolean();

            KiwiRunnables.runQuietly(() -> called.set(true));

            assertThat(called).isTrue();
        }

        @Test
        void shouldSuppressExceptions() {
            var called = new AtomicBoolean();

            KiwiRunnables.runQuietly(() -> {
                called.set(true);
                throw new Exception("oops");
            });

            assertThat(called).isTrue();
        }
    }

}
