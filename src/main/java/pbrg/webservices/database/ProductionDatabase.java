package pbrg.webservices.database;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ProductionDatabase {

    /** The initial context. */
    private static InitialContext initialContext;

    static {
        try {
            // Set the initial context to the default one
            setInitialContext(new InitialContext());
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the initial context.
     * @param initialContext The initial context (not null)
     */
    public static void setInitialContext(InitialContext initialContext) {
        if (initialContext == null) {
            throw new IllegalArgumentException("initialContext cannot be null");
        }
        ProductionDatabase.initialContext = initialContext;
    }


    /**
     * Check if the application is running in production.
     * @return true if the application is running in production, false otherwise
     */
    public static boolean production() {
        try {
            ((Context) initialContext.lookup("java:/comp/env"))
                .lookup("jdbc/grabourg");
            return true;
        } catch (NamingException exception) {
            return false;
        }
    }

    /**
     * Get the production DataSource. This method should only be called if the
     * application is running in production.
     * @return The production DataSource
     */
    public static DataSource productionDataSource() {
        try {
            return (DataSource) ((Context) initialContext.lookup("java:/comp/env"))
                .lookup("jdbc/grabourg");
        } catch (NamingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
