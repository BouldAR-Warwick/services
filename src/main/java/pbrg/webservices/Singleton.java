package pbrg.webservices;

import java.sql.Connection;
import java.sql.SQLException;

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
