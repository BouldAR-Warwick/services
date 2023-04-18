package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import pbrg.webservices.models.ChainingMysqlDataSource;

public final class TestDatabase {

    /** Store the Docker container ID of the test database. */
    private static String containerId;

    /** Database port. */
    private static final int DATABASE_PORT = 3306;

    /** Poll time to wait for the database to start. */
    private static final int BUSY_WAIT_MS = 100;

    /** Path to the docker-compose file to start the test database. */
    private static final String SCRIPT_PATH =
        "./scripts/database/start-mysql-test.yml";

    /**
     * Start the test database in a separate thread.
     * Runs run docker-compose -f ./scripts/start-mysql-test.yml up
     */
    private static final Runnable START_TEST_DATABASE = () -> {
        ProcessBuilder startDatabaseCommand = new ProcessBuilder(
                "docker-compose", "-f", SCRIPT_PATH, "up", "-d");
        try {
            Process startDatabase = startDatabaseCommand.start();
            try {
                startDatabase.waitFor();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            startDatabase.destroy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };

    /** Find the container ID of the test database. */
    private static final Runnable FIND_CONTAINER_ID = () -> {
        // Store container ID
        ProcessBuilder queryIDBuilder = new ProcessBuilder(
                "docker-compose", "-f", SCRIPT_PATH, "ps", "-q");
        Process queryID;
        try {
            queryID = queryIDBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(queryID.getInputStream()));
        try {
            containerId = reader.readLine();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        queryID.destroy();
    };

    /** Close the test database in a separate thread. */
    private static final Runnable CLOSE_CONTAINER = () -> {
        String command = "docker stop " + containerId;
        ProcessBuilder processBuilder = new ProcessBuilder(
                "/bin/bash", "-c", command);
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            assert exitCode == 0;
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    };

    /** Static class, no need to instantiate. */
    private TestDatabase() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the DataSource to the test database.
     * @return A DataSource to the test database
     */
    public static DataSource getTestDataSource() {
        // Use the MysqlDataSource as a DataSource
        // jdbc:mysql://localhost:3306/test_db
        return new ChainingMysqlDataSource()
                .setServerName("localhost")
                .setPort(DATABASE_PORT)
                .setDatabaseName("test_db")
                .setUser("test_user")
                .setPassword("test_password")
                .getMysqlDataSource();
    }

    /** Start the test database in a separate thread. */
    public static void startTestDatabaseInThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        run(executor, START_TEST_DATABASE);

        // await database startup
        DataSource dataSource = getTestDataSource();
        do {
            try {
                // sleep for BUSY_WAIT_MS milliseconds
                Thread.sleep(BUSY_WAIT_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } while (!dataSourceIsValid(dataSource));

        // Find container ID
        run(executor, FIND_CONTAINER_ID);

        // shutdown executor
        executor.shutdown();
    }

    /** Close the test database in a separate thread. */
    public static void closeTestDatabaseInThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        run(executor, CLOSE_CONTAINER);

        // shutdown executor
        executor.shutdown();
    }

    /**
     * Run a runnable in a separate thread.
     * @param executor executor service
     * @param closeContainer runnable
     * */
    private static void run(
        final ExecutorService executor, final Runnable closeContainer
    ) {
        Future<?> future = executor.submit(closeContainer);
        try {
            // wait for future to complete
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            // handle exception
        }
    }
}
