package pbrg.webservices.database;

import static pbrg.webservices.database.ProductionDatabase.production;
import static pbrg.webservices.database.ProductionDatabase.productionDataSource;

public final class DatabaseController {

    /** The data source. */
    private static javax.sql.DataSource dataSource;

    static {
        setDataSource(production());
    }

    /** Static class, no need to instantiate. */
    private DatabaseController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Set the data source.
     * @param inProduction true if in production, false otherwise
     */
    public static void setDataSource(final boolean inProduction) {
        if (inProduction) {
            setDataSource(productionDataSource());
        }
    }

    /**
     * Set the data source.
     * @param newDataSource data source
     */
    public static void setDataSource(
        final javax.sql.DataSource newDataSource
    ) {
        dataSource = newDataSource;
    }

    /**
     * Get the data source.
     * @return data source
     */
    public static javax.sql.DataSource getDataSource() {
        return dataSource;
    }
}
