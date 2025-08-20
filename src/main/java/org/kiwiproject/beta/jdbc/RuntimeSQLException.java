package org.kiwiproject.beta.jdbc;

import com.google.common.annotations.Beta;
import org.kiwiproject.base.KiwiPreconditions;

import java.sql.SQLException;

/**
 * Unchecked exception that wraps a {@link SQLException}.
 * <p>
 * <em>You should prefer kiwi's
 * {@link org.kiwiproject.jdbc.UncheckedSQLException UncheckedSQLException}
 * over this exception class for the reasons described below.</em>
 * <p>
 * This exception class was copied from kiwi-test. Later, kiwi version 4.2.0
 * added {@code UncheckedSQLException}. The {@code RuntimeSQLException}
 * in kiwi-test was then deprecated, scheduled for removal in version 5.0.0.
 * <p>
 * There are no current plans to deprecate or remove this class, but that
 * may change in a future release. So, we suggest changing any code using
 * this exception class to use kiwi's {@code UncheckedSQLException} instead.
 */
@Beta
public class RuntimeSQLException extends RuntimeException {

    /**
     * Constructs an instance of this class.
     *
     * @param message the detail message
     * @param cause   the {@link SQLException} which is the cause
     */
    public RuntimeSQLException(String message, SQLException cause) {
        super(message, KiwiPreconditions.requireNotNull(cause));
    }

    /**
     * Constructs an instance of this class.
     *
     * @param cause the {@link SQLException} which is the cause
     */
    public RuntimeSQLException(SQLException cause) {
        super(KiwiPreconditions.requireNotNull(cause));
    }

    /**
     * Returns the cause of this exception.
     *
     * @return the {@link SQLException} which is the cause of this exception.
     */
    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
