package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.*;
import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestDatabaseTest {
    @BeforeAll
    static void startResources() {
        startTestDatabaseInThread();
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @Test
    void test_getTestDataSource() {
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
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
}