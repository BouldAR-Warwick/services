package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.naming.InitialContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class DatabaseControllerTest {

    @BeforeAll
    static void startResources() throws IllegalStateException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());
    }

    @AfterAll
    static void closeResources() {
         closeTestDatabaseInThread();
    }

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<DatabaseController> constructor;
        try {
            constructor = DatabaseController.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("DatabaseUtils should have a private constructor");
            throw new RuntimeException(e);
        }

        // ensure calling constructor throws an IllegalStateException exception
        constructor.setAccessible(true);
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

    @Test
    void setDataSourceToProduction() {
        // given: in production
        boolean inProduction = true;

        // mock an InitialContext
        InitialContext prodContext =
            ProductionDatabaseTest.createProdContext();
        ProductionDatabase.setInitialContext(prodContext);

        // then: no exception is thrown
        assertDoesNotThrow(
            // when: setting the data source to the production database
            () -> DatabaseController.setDataSource(inProduction)
        );

        // after: reset context to use the test database, use the test database
        ProductionDatabase.setInitialContext(
            ProductionDatabase.getDefaultContext()
        );
        DatabaseController.setDataSource(getTestDataSource());
    }
}
