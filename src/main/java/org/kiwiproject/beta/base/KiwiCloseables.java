package org.kiwiproject.beta.base;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.beta.reflect.KiwiReflection2.isPublic;

import com.google.common.annotations.Beta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.beta.slf4j.KiwiSlf4j;
import org.slf4j.event.Level;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Static utilities related to {@link Closeable} and other objects that can be "closed", whether they
 * implement {@link Closeable} or not.
 */
@UtilityClass
@Slf4j
@Beta
public class KiwiCloseables {

    /**
     * A description of how to close a specific object.
     */
    @Accessors(fluent = true)
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CloseDescriptor {

        private static final String DEFAULT_CLOSE_METHOD_NAME = "close";

        /**
         * Contains the object to be closed, or {@code null}.
         */
        @Nullable
        private Object object;

        /**
         * The name of the "close" method.
         */
        private String closeMethodName;

        /**
         * Create an instance assuming the method to close the object is named "close".
         *
         * @param object to object to close
         * @return a new instance
         */
        public static CloseDescriptor of(@Nullable Object object) {
            return CloseDescriptor.of(object, DEFAULT_CLOSE_METHOD_NAME);
        }

        /**
         * Create an instance assuming the method to close the object is named
         * {@code closeMethodName}.
         *
         * @param object          the object to close
         * @param closeMethodName the name of the method that will close the object
         * @return a new instance
         */
        public static CloseDescriptor of(@Nullable Object object, String closeMethodName) {
            checkArgumentNotBlank(closeMethodName, "closeMethodName must not be blank");
            return (object instanceof CloseDescriptor closeDescriptor) ?
                    closeDescriptor : new CloseDescriptor(object, closeMethodName);
        }
    }

    /**
     * Close all the given objects, suppressing any exceptions. Suppressed exceptions are logged at WARN level.
     * <p>
     * Before using this method, consider whether it is safe to ignore exceptions thrown while closing an I/O resource.
     * It might be safe when using "input" objects such as when reading a file or an input stream, but it generally
     * is not safe when writing a file or to an output stream. Consider using {@link #closeAll(Object...)}.
     * <p>
     * The objects may be instances of {@link CloseDescriptor} to provide a custom name for the "close" method.
     * Otherwise, each object is assumed to use the default method name "close".
     *
     * @param objects the objects to close
     * @see #closeAll(Object...)
     */
    public static void closeAllQuietly(Object... objects) {
        validateNonNullVarargs(objects);
        Arrays.stream(objects)
                .filter(Objects::nonNull)
                .map(CloseDescriptor::of)
                .forEach(KiwiCloseables::closeQuietly);
    }

    /**
     * Close an object described by the {@link CloseDescriptor}, and suppress any exception. Suppressed exceptions
     * are logged at WARN level.
     * <p>
     * Before using this method, consider whether it is safe to ignore exceptions thrown while closing an I/O resource.
     * It might be safe when using "input" objects such as when reading a file or an input stream, but it generally
     * is not safe when writing a file or to an output stream. Consider using {@link #close(CloseDescriptor)}.
     * <p>
     * If the {@link CloseDescriptor} refers to a non-public method, this method will attempt to invoke it. But this
     * may result in an {@link IllegalAccessException}.
     *
     * @param descriptor the description of an object and its "close" method
     * @see #close(CloseDescriptor)
     */
    public static void closeQuietly(CloseDescriptor descriptor) {
        validateCloseDescriptor(descriptor);

        var object = descriptor.object();
        if (isNull(object)) {
            return;
        }

        if (object instanceof Closeable closeable) {
            closeQuietly(closeable);
            return;
        }

        var methodName = descriptor.closeMethodName();
        var exception = tryCloseObject(object, methodName);
        if (nonNull(exception)) {
            logExceptionClosingObject(Level.WARN, object, methodName, exception);
        }
    }

    /**
     * Close the given instance of {@link Closeable}, and suppress any exception. Suppressed exceptions
     * are logged at WARN level.
     * <p>
     * Before using this method, consider whether it is safe to ignore exceptions thrown while closing an I/O resource.
     * It might be safe when using "input" objects such as when reading a file or an input stream, but it generally
     * is not safe when writing a file or to an output stream. Consider using {@link #close(Closeable)}.
     *
     * @param closeable the {@link Closeable} to close
     * @see #close(Closeable)
     */
    public static void closeQuietly(@Nullable Closeable closeable) {
        if (isNull(closeable)) {
            return;
        }

        var exception = tryCloseCloseable(closeable);
        if (nonNull(exception)) {
            logExceptionClosingClosable(Level.WARN, closeable, exception);
        }
    }

    /**
     * Represents the result of an attempt to close an object.
     */
    @Accessors(fluent = true)
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CloseResult {

        /**
         * If true, the object was successfully closed.
         */
        private boolean closed;

        /**
         * If true, the object was null.
         */
        private boolean wasNull;

        /**
         * If an error occurred closing an object, this contains the {@link Exception} that was thrown.
         * If there was no error, this will be {@code null}.
         */
        @Nullable
        private Exception error;

        /**
         * Create a new "closed successfully" instance.
         *
         * @return a new instance that indicates that an object was successfully closed
         */
        public static CloseResult ofClosed() {
            return new CloseResult(true, false, null);
        }

        /**
         * Create a new "object was null" instance.
         *
         * @return a new instance that indicates an object was null (and thus not closed)
         */
        public static CloseResult ofNull() {
            return new CloseResult(false, true, null);
        }

        /**
         * Create a new "close failed" instance.
         *
         * @param exception the error that occurred closing an object
         * @return a new instance representing a failed close attempt, including the {@link Exception}
         */
        public static CloseResult ofError(Exception exception) {
            checkArgumentNotNull(exception);
            return new CloseResult(false, false, exception);
        }

        /**
         * @return true if the object was successfully closed or was null, otherwise false
         */
        public boolean closedOrWasNull() {
            return closed || wasNull;
        }

        /**
         * @return true if the close operation failed, otherwise false
         */
        public boolean hasError() {
            return nonNull(error);
        }
    }

    /**
     * Close all the given objects, and return a single {@link CloseResult} corresponding to each input object
     * in order.
     * <p>
     * The objects may be instances of {@link CloseDescriptor} to provide a custom name for the "close" method.
     * Otherwise, each object is assumed to use the default method name "close".
     *
     * @param objects the objects to close
     * @return a list of {@link CloseResult} containing the result for each attempted close operation
     */
    public static List<CloseResult> closeAll(Object... objects) {
        validateNonNullVarargs(objects);

        return Arrays.stream(objects)
                .map(CloseDescriptor::of)
                .map(KiwiCloseables::close)
                .toList();
    }

    private static void validateNonNullVarargs(Object... objects) {
        checkArgumentNotNull(objects, "don't cast varargs to null!");
    }

    /**
     * Close an object described by the {@link CloseDescriptor}.
     * <p>
     * If the {@link CloseDescriptor} refers to a non-public method, this method will attempt to invoke it. But this
     * may result in an {@link IllegalAccessException}.
     *
     * @param descriptor the description of an object and its "close" method
     * @return the {@link CloseResult}
     */
    public static CloseResult close(CloseDescriptor descriptor) {
        validateCloseDescriptor(descriptor);

        var object = descriptor.object();
        if (isNull(object)) {
            return CloseResult.ofNull();
        }

        if (object instanceof Closeable closeable) {
            return close(closeable);
        }

        var methodName = descriptor.closeMethodName();
        var exception = tryCloseObject(object, methodName);
        if (isNull(exception)) {
            return CloseResult.ofClosed();
        }

        logExceptionClosingObject(Level.TRACE, object, methodName, exception);
        return CloseResult.ofError(exception);
    }

    private static void validateCloseDescriptor(CloseDescriptor descriptor) {
        checkArgumentNotNull(descriptor);
        checkState(isNotBlank(descriptor.closeMethodName()), "invalid CloseDescriptor! closeMethodName is blank!");
    }

    private static Exception tryCloseObject(Object object, String methodName) {
        try {
            var method = object.getClass().getDeclaredMethod(methodName);
            setAccessibleIfNotPublic(method);
            method.invoke(object);
            return null;
        } catch (Exception e) {
            return e;
        }
    }

    private static void setAccessibleIfNotPublic(Method method) {
        if (!isPublic(method)) {
            method.setAccessible(true);
        }
    }

    private static void logExceptionClosingObject(Level level, Object object, String methodName, Exception exception) {
        KiwiSlf4j.log(LOG, level, "Suppressed exception thrown while closing {} from method {}",
                object.getClass().getName(), methodName, exception);
    }

    /**
     * Close the given instance of {@link Closeable}.
     *
     * @param closeable the {@link Closeable} to close
     * @return the {@link CloseResult}
     */
    public static CloseResult close(Closeable closeable) {
        if (isNull(closeable)) {
            return CloseResult.ofNull();
        }

        var exception = tryCloseCloseable(closeable);
        if (isNull(exception)) {
            return CloseResult.ofClosed();
        }

        logExceptionClosingClosable(Level.TRACE, closeable, exception);
        return CloseResult.ofError(exception);
    }

    private static void logExceptionClosingClosable(Level level, Closeable closeable, Exception exception) {
        KiwiSlf4j.log(LOG, level, "Suppressed exception thrown while closing Closeable ({})",
                closeable.getClass().getName(), exception);
    }

    private static Exception tryCloseCloseable(Closeable closeable) {
        try {
            closeable.close();
            return null;
        } catch (Exception e) {
            return e;
        }
    }
}
