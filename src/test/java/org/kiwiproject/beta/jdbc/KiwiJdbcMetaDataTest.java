package org.kiwiproject.beta.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.beta.jdbc.KiwiJdbcMetaData.resultSetContainsColumnLabel;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowingConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.test.h2.H2FileBasedDatabase;
import org.kiwiproject.test.junit.jupiter.H2Database;
import org.kiwiproject.test.junit.jupiter.H2FileBasedDatabaseExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@DisplayName("KiwiJdbcMetaData")
@ExtendWith(H2FileBasedDatabaseExtension.class)
@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
class KiwiJdbcMetaDataTest {

    private DataSource dataSource;

    @BeforeAll
    static void beforeAll(@H2Database H2FileBasedDatabase database) {
        withConnection(database.getDataSource(), conn -> {
            try (var st = conn.createStatement()) {
                st.execute("create table people (id integer, f_name varchar , l_name varchar, age integer)");
            }

            try (var ps = conn.prepareStatement("insert into people values (?, ?, ?, ?)")) {
                ps.setInt(1, 42);
                ps.setString(2, "Darrell");
                ps.setString(3, "Cartrip");
                ps.setInt(4, 39);
                ps.executeUpdate();
            }
        });
    }

    @BeforeEach
    void setUp(@H2Database H2FileBasedDatabase database) {
        dataSource = database.getDataSource();
    }

    @Nested
    class ResultSetContainsColumnLabel {

        @Test
        void shouldReturnExpectedMatches() {
            withConnection(conn -> {
                try (var st = conn.createStatement();
                     var rs = st.executeQuery("select id, f_name as first_name, l_name as last_name, age from people where id = 42")) {
                    assertAll(
                            () -> assertThat(resultSetContainsColumnLabel(rs, "id")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "f_name")).isFalse(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "first_name")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "l_name")).isFalse(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "last_name")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "age")).isTrue()
                    );
                }
            });
        }

        @Test
        void shouldBeCaseInsensitive() {
            withConnection(conn -> {
                try (var st = conn.createStatement();
                     var rs = st.executeQuery("select id, f_name as first_name, l_name as last_name, age from people where id = 42")) {
                    assertAll(
                            () -> assertThat(resultSetContainsColumnLabel(rs, "ID")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "FIRST_NAME")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "LAST_NAME")).isTrue(),
                            () -> assertThat(resultSetContainsColumnLabel(rs, "AGE")).isTrue()
                    );
                }
            });
        }

        @Test
        void shouldThrowRuntimeSQLException_WhenResultSet_ThrowsSQLExceptionGettingMetaData() throws SQLException {
            // noinspection resource
            var rs = mock(ResultSet.class);
            var reason = "MetaData not supported for some reason";
            when(rs.getMetaData()).thenThrow(new SQLException(reason));

            assertThatThrownBy(() -> resultSetContainsColumnLabel(rs, "created_at"))
                    .isExactlyInstanceOf(RuntimeSQLException.class)
                    .hasCauseInstanceOf(SQLException.class)
                    .hasMessageContaining(reason);
        }

        @Test
        void shouldThrowRuntimeSQLException_WhenMetaData_ThrowsSQLExceptionGettingLabel() throws SQLException {
            var metaData = mock(ResultSetMetaData.class);
            when(metaData.getColumnCount()).thenReturn(42);
            var reason = "No column label for you!";
            when(metaData.getColumnLabel(anyInt())).thenThrow(new SQLException(reason));

            // noinspection resource
            var rs = mock(ResultSet.class);
            when(rs.getMetaData()).thenReturn(metaData);

            assertThatThrownBy(() -> resultSetContainsColumnLabel(rs, "created_at"))
                    .isExactlyInstanceOf(RuntimeSQLException.class)
                    .hasCauseInstanceOf(SQLException.class)
                    .hasMessageContaining(reason);
        }
    }

    private void withConnection(ThrowingConsumer<Connection> connectionConsumer) {
        withConnection(dataSource, connectionConsumer);
    }

    private static void withConnection(DataSource dataSource, ThrowingConsumer<Connection> connectionConsumer) {
        try (var conn = dataSource.getConnection()) {
            connectionConsumer.accept(conn);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }
}
