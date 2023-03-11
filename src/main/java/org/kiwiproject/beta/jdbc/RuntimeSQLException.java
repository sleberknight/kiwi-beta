package org.kiwiproject.beta.jdbc;

import com.google.common.annotations.Beta;
import org.kiwiproject.base.KiwiPreconditions;

import java.sql.SQLException;

/**
 * Unchecked exception that wraps a {@link SQLException}.
 * <p>
 * Note: This was copied from kiwi-test. I am not sure why we
 * put this in kiwi-test instead of kiwi. It should probably
 * be moved to kiwi, then maybe deprecated and removed from
 * kiwi-test, though it would not hurt anything to leave it.
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
