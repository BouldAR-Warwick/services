package pbrg.webservices;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public final class Singleton {
    private static Singleton INSTANCE;
    private String info = "For services storage, db";

    private static Context ctx;
    private static DataSource ds;
    private static Connection conn;

    public static String wallImagePath = System.getProperty("user.home") + "/wall-images/";   

    private static Map<String, String> contentTypeLookup = Map.ofEntries(
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
    
    public static Singleton getInstance() {
        if (Singleton.INSTANCE == null) {
            Singleton.INSTANCE = new Singleton();
        }
        
        return Singleton.INSTANCE;
    }

    public static String getContentType(String imageFormat) {
        return Singleton.contentTypeLookup.get(imageFormat);
    }

    /** Instantiate DB */
    private static void instantiateDB() throws NamingException {
        Singleton.ctx = new InitialContext();
        Singleton.ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/grabourg");
    }

    /** Get DB connection */
    public static Connection getDbConnection() {
        // if unclosed connection -> close it
        if (Singleton.conn != null) {
            Singleton.closeDbConnection();
        }

        // create and return new connection
        try {
            Singleton.conn = Singleton.ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Singleton.conn;
    }

    /** Close DB connection */
    public static void closeDbConnection() {
        if (Singleton.conn == null) {
            return;
        }

        try {
            Singleton.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // getters and setters
}
