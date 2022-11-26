package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * For static utils: \ getting database connection, file path utils, API utils.
 */
public final class Utils {

    /**
     * Path to wall image directory.
     */
    public static final String WALL_IMAGE_PATH =
        System.getProperty("user.home") + "/wall-images/";

    /**
     * Path to route image directory.
     */
    public static final String ROUTE_IMAGE_PATH =
        System.getProperty("user.home") + "/route-images/";

    /**
     * Map from file extensions to content types.
     */
    private static final Map<String, String> CONTENT_TYPE_MAP =
        Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("pbg", "image/png")
          /*
          unsupported:
              image/gif
              image/tiff
              image/vnd.microsoft.icon
              image/x-icon
              image/vnd.djvu
              image/svg+xml
          */
        );

    private Utils() {
    }

    /**
     * Get a content type from a file extension.
     *
     * @param imageFormat file extension
     * @return content type
     */
    public static String getContentType(final String imageFormat) {
        return CONTENT_TYPE_MAP.get(imageFormat);
    }

    /**
     * Get DB connection.
     *
     * @return DB connection
     */
    public static Connection getDbConnection() {
        // TODO - pass implementation properties to InitialContext
        Connection connection = null;
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/grabourg");

            // create and return new connection
            connection = ds.getConnection();
        } catch (NamingException | SQLException exception) {
            System.out.println(exception.getMessage());
        }

        assert (connection != null);
        return connection;
    }
}
