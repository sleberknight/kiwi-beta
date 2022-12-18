package org.kiwiproject.beta.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

@DisplayName("RuntimeSQLException")
class RuntimeSQLExceptionTest {

    // NOTE:
    // When comparing SQLException, some shenanigans is necessary because SQLException
    // extends Exception and implements Iterable<Throwable>, so it causes ambiguity in the
    // call to AssertJ's assertThat. By explicitly declaring the type to be Throwable, we
    // can inform AssertJ how we want to perform the comparison. I have no idea whatsoever
    // why the usual Exception causal chain mechanism wasn't good enough for SQLException.
    // It was introduced in Java 1.6, and I can't find any reason or historical references
    // for this design choice. It seems completely unnecessary, since you can chain exceptions
    // via their cause.

    @Test
    void shouldConstructWithSQLException() {
        var sqlEx = new SQLException("FK violation");
        var runtimeSQLException = new RuntimeSQLException(sqlEx);

        assertThat(runtimeSQLException.getMessage()).contains(sqlEx.getMessage());
        Throwable cause = runtimeSQLException.getCause();  // see NOTE
        assertThat(cause).isSameAs(sqlEx);
    }

    @Test
    void shouldConstructWithMessageAndSQLException() {
        var sqlEx = new SQLException("Unique constraint violation");
        var runtimeSQLException = new RuntimeSQLException("Wrapper", sqlEx);

        assertThat(runtimeSQLException.getMessage()).isEqualTo("Wrapper");
        Throwable cause = runtimeSQLException.getCause();  // see NOTE
        assertThat(cause).isSameAs(sqlEx);
    }
}
