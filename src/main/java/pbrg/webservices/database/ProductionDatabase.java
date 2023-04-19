package pbrg.webservices.database;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ProductionDatabase {

    /** Static class, no need to instantiate. */
    private ProductionDatabase() {
        throw new IllegalStateException("Utility class");
    }

    /** The initial context. */
    private static InitialContext initialContext;

    static {
        // Set the initial context to the default one
        setInitialContext(getDefaultContext());
    }

    /**
     * Get the default initial context.
     * @return The default initial context
     */
    @Contract(" -> new")
    static @NotNull InitialContext getDefaultContext() {
        return getDefaultContext(null);
    }

    /**
     * Get the initial context with an environment.
     * @param environment The environment
     * @return The default initial context
     */
    @Contract("_ -> new")
    static @NotNull InitialContext getDefaultContext(
        final Properties environment
    ) {
        try {
            return new InitialContext(environment);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the initial context.
     * @param newInitialContext The initial context (not null)
     */
    public static void setInitialContext(
        final InitialContext newInitialContext
    ) {
        if (newInitialContext == null) {
            throw new IllegalArgumentException("initialContext cannot be null");
        }
        ProductionDatabase.initialContext = newInitialContext;
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
            return (DataSource) (
                (Context) initialContext.lookup("java:/comp/env")
            ).lookup("jdbc/grabourg");
        } catch (NamingException exception) {
            throw new RuntimeException(exception);
        }
    }
}
