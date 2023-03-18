package org.kiwiproject.beta.concurrent;

import lombok.experimental.UtilityClass;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@UtilityClass
public class KiwiFutures {

    public static <V> V resultNow(Future<V> future) {
        // see JDK 19 code, which I have basically copied and slightly modified and reformatted:
        // https://github.com/openjdk/jdk19/blob/master/src/java.base/share/classes/java/util/concurrent/Future.java

        // Questions:
        // - Why do they have the (infinite) loop?
        // - How does the loop exit if the #get method throws InterruptedException?
        // - What would happen if the loop is removed such that only the try/catch exists, and assuming the #interrupt
        //   call is moved into the InteruptedException catch block?

        if (isNotDone(future)) {
            throw new IllegalStateException("Task has not completed");
        }

        var interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Task completed with exception", e.getCause());
                } catch (CancellationException e) {
                    throw new IllegalStateException("Task was cancelled", e);
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static <V> Throwable exceptionNow(Future<V> future) {
        if (isNotDone(future)) {
            throw new IllegalStateException("Task has not completed");
        }

        if (future.isCancelled()) {
            throw new IllegalStateException("Task was cancelled");
        }

        var interrupted = false;
        try {
            while (true) {
                try {
                    future.get();
                    throw new IllegalStateException("Task completed with a result");
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    return e.getCause();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static <V> boolean isNotDone(Future<V> future) {
        return !future.isDone();
    }
}
