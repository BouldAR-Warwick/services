package pbrg.webservices.database;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public final class DatabaseUtils {

    /** Utility class, no need to instantiate. */
    private DatabaseUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if the DataSource is valid (can retrieve a non-null connection).
     * @param dataSource The DataSource to check
     * @return true if the DataSource is valid, false otherwise
     */
    public static boolean dataSourceIsValid(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection != null;
        } catch (SQLException | NullPointerException e) {
            // Connection failed
            return false;
        }
    }

}
