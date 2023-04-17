package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProductionDatabaseTest {

    /** Mock an initial context with production database. */
    private static InitialContext prodContext;

    /** Mock an initial context without production database. */
    private static InitialContext noProdContext;

    @BeforeAll
    static void createMockContexts() throws NamingException {
        prodContext = mock(InitialContext.class);
        when(prodContext.lookup("java:/comp/env")).thenReturn(prodContext);
        when(prodContext.lookup("jdbc/grabourg")).thenReturn(mock(DataSource.class));

        noProdContext = mock(InitialContext.class);
        when(noProdContext.lookup("java:/comp/env")).thenReturn(noProdContext);
        when(noProdContext.lookup("jdbc/grabourg"))
            .thenThrow(new NamingException("No production database"));
    }

    @AfterEach
    void resetContext() throws NamingException {
        ProductionDatabase.setInitialContext(new InitialContext());
    }

    @Test
    void inProduction() {
        // given, when: in production
        ProductionDatabase.setInitialContext(prodContext);

        // then: prod method should detect
        assertTrue(ProductionDatabase.production());
    }

    @Test
    void notInProduction() {
        // given, when: not in production
        ProductionDatabase.setInitialContext(noProdContext);

        // then: prod method should detect
        assertFalse(ProductionDatabase.production());
    }

    @Test
    void getProductionDataSourceInProd() {
        // given, when: in production
        ProductionDatabase.setInitialContext(prodContext);

        // then: should be able to get data source without exception
        assertDoesNotThrow(ProductionDatabase::productionDataSource);
    }

    @Test
    void getProductionDataSourceNotInProd() {
        // given, when: not in production
        ProductionDatabase.setInitialContext(noProdContext);

        // then: getting data source should throw NamingException
        assertThrows(RuntimeException.class, ProductionDatabase::productionDataSource);
    }
}