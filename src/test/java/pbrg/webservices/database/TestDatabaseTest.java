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
import static pbrg.webservices.utils.TestUtils.dockerDaemonRunning;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TestDatabaseTest {
    @BeforeAll
    static void startResources() {
        assertDoesNotThrow(
            TestDatabase::startTestDatabaseInThread
        );
    }

    @AfterAll
    static void closeResources() {
        assertDoesNotThrow(
            TestDatabase::closeTestDatabaseInThread
        );
    }

    @Test
    void testPrivateConstructor() {
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
    void startTestDatabaseInThreadDockerDaemonInactive() {
        // given: system where Docker daemon is not running

        // use reflection to access and set dockerIsRunning to false
        Field dockerIsRunningField;
        try {
            dockerIsRunningField = TestDatabase.class.getDeclaredField(
                "dockerIsRunning"
            );
        } catch (NoSuchFieldException e) {
            fail("dockerIsRunning field should exist");
            throw new RuntimeException(e);
        }

        dockerIsRunningField.setAccessible(true);
        try {
            dockerIsRunningField.set(null, false);
        } catch (IllegalAccessException e) {
            fail("Could not set dockerIsRunning");
        }

        assertThrows(
            // then: an IllegalStateException should be thrown
            IllegalStateException.class,

            // when: startTestDatabaseInThread is called
            TestDatabase::startTestDatabaseInThread
        );

        // after: reset dockerIsRunning
        try {
            dockerIsRunningField.set(null, dockerDaemonRunning());
        } catch (IllegalAccessException e) {
            fail("Could not set dockerIsRunning");
        }
    }

    @Test
    void awaitDatabaseStartupTestInterruptedException()
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
        assertDoesNotThrow(testThread::interrupt);

        // Wait for the testThread to finish
        testThread.join();
    }

    @Test
    void findContainerIDMultiLineOutput()
        throws NoSuchFieldException, IllegalAccessException {
        // given: a process whose output contains multiple lines

        // process builder that echoes a multi-line string
        ProcessBuilder multiLineOutput = new ProcessBuilder(
            "bash", "-c", "echo '\n'"
        );

        // use reflection on ProcessBuilder queryIDCommand
        Field queryIDCommand = TestDatabase.class.getDeclaredField(
            "queryIDCommand"
        );
        queryIDCommand.setAccessible(true);
        ProcessBuilder originalQueryIDCommand =
            (ProcessBuilder) queryIDCommand.get(null);

        // inject queryIDCommand with the multiLineOutput
        try {
            queryIDCommand.set(null, multiLineOutput);
        } catch (IllegalAccessException e) {
            fail("Could not set queryIDCommand");
        }

        // reflection to get FIND_CONTAINER_ID runnable
        Field findContainerIDField = TestDatabase.class.getDeclaredField(
            "FIND_CONTAINER_ID"
        );
        findContainerIDField.setAccessible(true);
        Runnable findContainerID = (Runnable) findContainerIDField.get(null);

        assertThrows(
            // then: runtime exception should be thrown
            RuntimeException.class,

            // when: the container ID is queried
            findContainerID::run
        );

        // after: reset queryIDCommand
        queryIDCommand.set(null, originalQueryIDCommand);
    }

    @Test
    void testRunProcessBuilder()
        throws InterruptedException, IOException, NoSuchMethodException,
        IllegalAccessException {
        // given: a process that has a non-zero exit value
        Process process = mock(Process.class);
        when(process.waitFor()).thenReturn(1);
        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        when(processBuilder.start()).thenReturn(process);

        // reflection to get the runProcessBuilder method
        Method runProcessBuilder = TestDatabase.class.getDeclaredMethod(
            "runProcessBuilder", ProcessBuilder.class, boolean.class
        );
        runProcessBuilder.setAccessible(true);

        try {
            // when: the process is started
            runProcessBuilder.invoke(null, processBuilder, false);
        } catch (InvocationTargetException e) {
            // then: an exception should be thrown
            assertTrue(
                e.getCause() instanceof RuntimeException,
                "Should throw RuntimeException "
                    + "(process has non-zero exit value)"
            );
        }
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
