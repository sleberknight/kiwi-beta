package org.kiwiproject.beta.jdbc;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.stream.IntStream;

/**
 * Utilities relating to JDBC metadata.
 */
@Beta
@UtilityClass
public class KiwiJdbcMetaData {

    /**
     * Check whether a {@link ResultSet} contains a column label, ignoring case since some database systems (e.g. H2)
     * return column metadata in all capitals, e.g. {@code FIRST_NAME} instead of {@code first_name}.
     *
     * @param rs          the ResultSet to check
     * @param columnLabel the column label to look for
     * @return true if the ResultSet contains the given column label
     * @throws RuntimeSQLException if there was any error getting column metadata
     */
    public static boolean resultSetContainsColumnLabel(ResultSet rs, String columnLabel) {
        // NOTE: column numbers in JDBC result sets are 1-based, i.e. 1, 2, ..., N
        try {
            var metaData = rs.getMetaData();
            var columnCount = metaData.getColumnCount();
            var foundColNum = IntStream.rangeClosed(1, columnCount)
                    .filter(colNum -> metaDataColumnLabelEquals(metaData, colNum, columnLabel))
                    .findFirst()
                    .orElse(0);
            return foundColNum > 0;
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);
        }
    }

    private static boolean metaDataColumnLabelEquals(ResultSetMetaData metaData, int colNum, String columnLabel) {
        try {
            return metaData.getColumnLabel(colNum).equalsIgnoreCase(columnLabel);
        } catch (SQLException e) {
            throw new RuntimeSQLException(e);  // this really should never happen, so just wrap in a runtime exception
        }
    }

}
