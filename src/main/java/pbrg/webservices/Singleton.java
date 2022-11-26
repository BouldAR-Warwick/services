package pbrg.webservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/** For services storage - e.g., db */
public final class Singleton {
    private static DataSource ds;
    private static Connection conn;

    public static final String wallImagePath = System.getProperty("user.home") + "/wall-images/";

    private static final Map<String, String> contentTypeLookup = Map.ofEntries(
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

    static {
        try {
            instantiateDB();
        } catch (NamingException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private Singleton() {

    }

    public static String getContentType(String imageFormat) {
        return Singleton.contentTypeLookup.get(imageFormat);
    }

    /** Instantiate DB */
    private static void instantiateDB() throws NamingException {
        // TODO - pass implementation properties to InitialContext
        Context initContext = new InitialContext();
        Context envContext = (Context) initContext.lookup("java:/comp/env");
        ds = (DataSource) envContext.lookup("jdbc/grabourg");
    }

    /** Get DB connection */
    public static Connection getDbConnection() throws SQLException {
        // if unclosed connection -> close it
        if (Singleton.conn != null) {
            Singleton.closeDbConnection();
        }

        if (ds == null) {
            System.out.println("DB not initialised");
            System.exit(1);
        }

        // create and return new connection
        Singleton.conn = Singleton.ds.getConnection();
        return Singleton.conn;
    }

    /** Close DB connection */
    public static void closeDbConnection() throws SQLException {
        if (Singleton.conn == null) {
            return;
        }

        Singleton.conn.close();
    }

    // getters and setters
}
