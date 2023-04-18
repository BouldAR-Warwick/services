package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TestDatabaseTest {
    @BeforeAll
    static void startResources() {
        startTestDatabaseInThread();
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @Test
    public void testPrivateConstructor() {
        // get constructor
        Constructor<DatabaseUtils> constructor;
        try {
            constructor = DatabaseUtils.class.getDeclaredConstructor();
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
    void testGetTestDataSource() {
        DataSource dataSource = getTestDataSource();
        assertNotNull(dataSource);
        assert dataSourceIsValid(dataSource);

        assertDoesNotThrow(() -> {
            // Get a connection from the DataSource
            Connection connection = dataSource.getConnection();
            assertNotNull(connection);
            connection.close();
        }, "getConnection should not throw an SQLException");
    }

    @Test
    void testRunThrowingException() throws NoSuchMethodException {
        // get the run method using reflection
        Method runMethod = TestDatabase.class.getDeclaredMethod(
            "run", ExecutorService.class, Runnable.class
        );
        runMethod.setAccessible(true);

        // create an executor
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // mock the closeContainer to throw an exception
        Runnable closeContainer = () -> {
            throw new RuntimeException("test exception");
        };

        // invoke the run method with the executor and mocked closeContainer
        assertDoesNotThrow(
            () -> runMethod.invoke(null, executor, closeContainer)
        );

        // shutdown executor
        executor.shutdown();
    }
}
