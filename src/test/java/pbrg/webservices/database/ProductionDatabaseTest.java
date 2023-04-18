package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ProductionDatabaseTest {

    @Test
    public void testPrivateConstructor() {
        // get constructor
        Constructor<ProductionDatabase> constructor;
        try {
            constructor = ProductionDatabase.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("DatabaseUtils should have a private constructor");
            throw new RuntimeException(e);
        }
        constructor.setAccessible(true);

        // ensure calling constructor throws an IllegalStateException exception
        try {
            constructor.newInstance();
            fail("Expected IllegalStateException to be thrown");
        } catch (
            InvocationTargetException | InstantiationException
            | IllegalAccessException e
        ) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    /**
     * Mock an initial context with production database.
     * @return The mock initial context
     */
    static InitialContext createProdContext() {
        InitialContext prodContext = mock(InitialContext.class);

        try {
            when(prodContext.lookup("java:/comp/env"))
                .thenReturn(prodContext);
            when(prodContext.lookup("jdbc/grabourg"))
                .thenReturn(mock(DataSource.class));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        assertDoesNotThrow(
            () -> {
                prodContext.lookup("java:/comp/env");
                prodContext.lookup("jdbc/grabourg");
            }
        );
        return prodContext;
    }

    /**
     * Mock an initial context without production database.
     * @return The mock initial context
     */
    private static InitialContext createNotProdContext() {
        InitialContext noProdContext = mock(InitialContext.class);

        try {
            when(noProdContext.lookup("java:/comp/env"))
                .thenReturn(noProdContext);
            when(noProdContext.lookup("jdbc/grabourg"))
                .thenThrow(new NamingException("No production database"));
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        assertThrows(
            NamingException.class,
            () -> {
                noProdContext.lookup("java:/comp/env");
                noProdContext.lookup("jdbc/grabourg");
            }
        );
        return noProdContext;
    }

    @AfterEach
    void resetContext() {
        ProductionDatabase.setInitialContext(
            ProductionDatabase.getDefaultContext()
        );
    }

    @Test
    void inProduction() {
        // given, when: in production
        ProductionDatabase.setInitialContext(createProdContext());

        // then: prod method should detect
        assertTrue(ProductionDatabase.production());
    }

    @Test
    void notInProduction() {
        // given, when: not in production
        ProductionDatabase.setInitialContext(createNotProdContext());

        // then: prod method should detect
        assertFalse(ProductionDatabase.production());
    }

    @Test
    void getProductionDataSourceInProd() {
        // given, when: in production
        ProductionDatabase.setInitialContext(createProdContext());

        // then: should be able to get data source without exception
        assertDoesNotThrow(ProductionDatabase::productionDataSource);
    }

    @Test
    void getProductionDataSourceNotInProd() {
        // given, when: not in production
        ProductionDatabase.setInitialContext(createNotProdContext());

        assertThrows(
            // then: should throw RuntimeException
            RuntimeException.class,

            // when: getting production data source
            ProductionDatabase::productionDataSource
        );
    }

    @Test
    void setInitialContextNull() {
        // given, when: null context
        // then: should throw IllegalArgumentException
        assertThrows(
            IllegalArgumentException.class,
            () -> ProductionDatabase.setInitialContext(null)
        );
    }

    @Test
    void testGetDefaultContextPassing() {
        // Test for successful InitialContext creation
        assertDoesNotThrow(() -> ProductionDatabase.getDefaultContext());
    }

    @Test
    void testGetDefaultContextFailing() {
        // given: an invalid environment
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "invalidValue");

        // Test for RuntimeException when NamingException is thrown
        assertThrows(
            // then: should throw RuntimeException
            RuntimeException.class,

            // when: creating InitialContext with invalid environment
            () -> ProductionDatabase.getDefaultContext(env)
        );
    }
}
