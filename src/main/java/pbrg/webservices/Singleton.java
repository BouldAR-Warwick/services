package pbrg.webservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * For services storage - e.g., db
 */
public final class Singleton {

    public static final String wallImagePath = System.getProperty("user.home") + "/wall-images/";

    public static final String routeImagePath = System.getProperty("user.home") + "/route-images/";
    private static final Map<String, String> contentTypeLookup =
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

    private Singleton() {
    }

    public static String getContentType(String imageFormat) {
        return contentTypeLookup.get(imageFormat);
    }

    /**
     * Get DB connection
     */
    public static Connection getDbConnection() {
        // TODO - pass implementation properties to InitialContext
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/grabourg");

            // create and return new connection
            return ds.getConnection();
        } catch (NamingException | SQLException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }
}
