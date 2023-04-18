package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import javax.sql.DataSource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TestDatabaseTest {
    @BeforeAll
    static void startResources() {

    }

    @AfterAll
    static void closeResources() {

    }

    @Test
    public void testPrivateConstructor() {
        // get constructor
        Constructor<TestDatabase> constructor;
        try {
            constructor = TestDatabase.class.getDeclaredConstructor();
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
        startTestDatabaseInThread();

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
    public void awaitDatabaseStartupTestInterruptedException()
        throws SQLException, NoSuchMethodException, InterruptedException {
        // use reflection to access the private method
        Method awaitDatabaseStartup = TestDatabase.class.getDeclaredMethod(
            "awaitDatabaseStartup", DataSource.class
        );
        awaitDatabaseStartup.setAccessible(true);

        // mock an invalid datasource
        DataSource invalidDataSource = mock(DataSource.class);
        when(invalidDataSource.getConnection()).thenThrow(SQLException.class);

        Thread testThread = new Thread(() -> {
            try {
                // Call the method that contains the sleep
                awaitDatabaseStartup.invoke(null, invalidDataSource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        testThread.start();

        // Interrupt the testThread while it is sleeping
        testThread.interrupt();

        // Wait for the testThread to finish
        testThread.join();
    }

    @SuppressWarnings("unchecked")
    static @NotNull Function<String, ProcessBuilder>
        injectCreateDockerStopCommand(final ProcessBuilder mockedCommand)
        throws NoSuchFieldException, IllegalAccessException {
        // reflection to change createDockerStopCommand
        Field createDockerStopCommandField =
            TestDatabase.class.getDeclaredField("createDockerStopCommand");
        createDockerStopCommandField.setAccessible(true);

        Function<String, ProcessBuilder> originalFunction;
        Object fieldValue = createDockerStopCommandField.get(null);
        if (!(fieldValue instanceof Function)) {
            throw new RuntimeException("Unexpected field value");
        }
        originalFunction = (Function<String, ProcessBuilder>) fieldValue;

        // mock function with process builder
        Function<String, ProcessBuilder> mockedFunction =
            (thisContainerId) -> mockedCommand;
        createDockerStopCommandField.set(null, mockedFunction);

        return originalFunction;
    }

    static void replaceOriginalCreateDockerStopCommand(
        final Function<String, ProcessBuilder> originalFunction
    ) throws NoSuchFieldException, IllegalAccessException {
        // reflection to retrieve createDockerStopCommand
        Field createDockerStopCommandField =
            TestDatabase.class.getDeclaredField("createDockerStopCommand");
        createDockerStopCommandField.setAccessible(true);

        // replace the original function
        createDockerStopCommandField.set(null, originalFunction);
    }

    @Test
    void testRunnableCloseContainerNonZeroExitCode()
        throws IOException, NoSuchFieldException,
        IllegalAccessException, InterruptedException {
        // given: a process that has a non-zero exit value
        Process process = mock(Process.class);
        when(process.waitFor()).thenReturn(1);
        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        when(processBuilder.start()).thenReturn(process);

        // inject the mocked process builder
        Function<String, ProcessBuilder> originalFunction =
            injectCreateDockerStopCommand(processBuilder);

        // reflection to get the CLOSE_CONTAINER Runnable
        Field closeContainerField = TestDatabase.class.getDeclaredField(
            "CLOSE_CONTAINER"
        );
        closeContainerField.setAccessible(true);
        Runnable closeContainer = (Runnable) closeContainerField.get(null);

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the process is started
            closeContainer::run,
            "Expected RuntimeException to be thrown"
        );

        // after: the original function should be restored
        replaceOriginalCreateDockerStopCommand(originalFunction);
    }

    @Test
    void testRunThrowingException() throws NoSuchMethodException {
        // get the run method using reflection
        Method run = TestDatabase.class.getDeclaredMethod(
            "run", ExecutorService.class, Runnable.class
        );
        run.setAccessible(true);

        // create an executor
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // mock the closeContainer to throw an exception
        Runnable closeContainer = () -> {
            throw new RuntimeException("test exception");
        };

        // invoke the run method with the executor and mocked closeContainer
        assertDoesNotThrow(
            () -> run.invoke(null, executor, closeContainer)
        );

        // shutdown executor
        executor.shutdown();
    }
}
