package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;
import static pbrg.webservices.utils.Utils.collectOutputAsList;

import java.io.IOException;
import java.util.List;
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

    /** query the container ID of the test database. */
    @SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
    private static ProcessBuilder queryIDCommand =
        new ProcessBuilder("docker-compose", "-f", SCRIPT_PATH, "ps", "-q");

    /**
     * Start the test database in a separate thread.
     * Runs run docker-compose -f ./scripts/start-mysql-test.yml up
     */
    private static final Runnable START_TEST_DATABASE = () -> {
        ProcessBuilder startDatabaseCommand = new ProcessBuilder(
            "docker-compose", "-f", SCRIPT_PATH, "up", "-d"
        );
        runProcessBuilder(startDatabaseCommand);
    };

    /** Find the container ID of the test database. */
    private static final Runnable FIND_CONTAINER_ID = () -> {
        List<String> output = runProcessBuilder(
            queryIDCommand, true
        );
        if (output.size() != 1) {
            throw new RuntimeException("Expected one line of output");
        }
        containerId = output.get(0);
    };

    /** Close the test database in a separate thread. */
    private static final Runnable CLOSE_CONTAINER = () -> {
        // stop a Docker container by ID
        ProcessBuilder closeContainerCommand = new ProcessBuilder(
            "/bin/bash", "-c", "docker stop " + containerId
        );
        runProcessBuilder(closeContainerCommand);
    };

    /**
     * Run the process builder command.
     * @param command The command to run
     */
    private static void runProcessBuilder(
        final ProcessBuilder command
    ) {
        runProcessBuilder(command, false);
    }

    /**
     * Run the process builder command.
     * @param command The command to run
     * @param collectOutput Whether to collect the output of the command
     * @return The output of the command
     */
    private static List<String> runProcessBuilder(
        final ProcessBuilder command,
        final boolean collectOutput
    ) {
        List<String> output = null;
        try {
            Process process = command.start();
            if (collectOutput) {
                output = collectOutputAsList(process);
            }
            int exitCode = process.waitFor();
            process.destroy();
            if (exitCode != 0) {
                throw new InterruptedException(
                    "Command failed (non-zero exit code)"
                );
            }
            return output;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

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
        awaitDatabaseStartup(dataSource);

        // Find container ID
        run(executor, FIND_CONTAINER_ID);

        // shutdown executor
        executor.shutdown();
    }

    /**
     * Await database startup.
     * @param dataSource DataSource to the database
     */
    private static void awaitDatabaseStartup(
        final DataSource dataSource
    ) {
        do {
            try {
                // sleep for BUSY_WAIT_MS milliseconds
                Thread.sleep(BUSY_WAIT_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        } while (!dataSourceIsValid(dataSource));
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