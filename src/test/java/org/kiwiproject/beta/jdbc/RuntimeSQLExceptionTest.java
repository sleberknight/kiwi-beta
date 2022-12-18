package org.kiwiproject.beta.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

@DisplayName("RuntimeSQLException")
class RuntimeSQLExceptionTest {

    @Test
    void shouldConstructWithSQLException() {
        var sqlEx = new SQLException("FK violation");
        var runtimeSQLException = new RuntimeSQLException(sqlEx);

        assertThat(runtimeSQLException)
                .hasMessageContaining(sqlEx.getMessage())
                .hasCauseReference(sqlEx);
    }

    @Test
    void shouldConstructWithMessageAndSQLException() {
        var sqlEx = new SQLException("Unique constraint violation");
        var runtimeSQLException = new RuntimeSQLException("Wrapper", sqlEx);

        assertThat(runtimeSQLException)
                .hasMessage("Wrapper")
                .hasCauseReference(sqlEx);
    }
}
