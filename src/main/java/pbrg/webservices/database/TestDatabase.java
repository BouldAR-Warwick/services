package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseUtils.dataSourceIsValid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.sql.DataSource;
import pbrg.webservices.models.ChainingMysqlDataSource;

public class TestDatabase {

    /** Store the Docker container ID of the test database. */
    private static String containerId;

    /** Path to the docker-compose file to start the test database. */
    private static final String scriptPath = "./scripts/database/start-mysql-test.yml";

    /**
     * Start the test database in a separate thread.
     * Runs run docker-compose -f ./scripts/start-mysql-test.yml up
     */
    private static final Runnable startTestDatabase = () -> {
        ProcessBuilder startDatabaseCommand = new ProcessBuilder(
            "docker-compose", "-f", scriptPath, "up", "-d"
        );
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
    private static final Runnable findContainerID = () -> {
        // Store container ID
        ProcessBuilder queryIDBuilder = new ProcessBuilder(
            "docker-compose", "-f", scriptPath, "ps", "-q"
        );
        Process queryID;
        try {
            queryID = queryIDBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(queryID.getInputStream())
        );
        try {
            containerId = reader.readLine();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        queryID.destroy();
    };

    /** Close the test database in a separate thread. */
    private static final Runnable closeContainer = () -> {
        String command = "docker stop " + containerId;
        ProcessBuilder processBuilder = new ProcessBuilder(
            "/bin/bash", "-c", command
        );
        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            assert exitCode == 0;
            process.destroy();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    };


    /**
     * Get the DataSource to the test database.
     * @return A DataSource to the test database
     */
    public static DataSource getTestDataSource() {
        // Use the MysqlDataSource as a DataSource
        // jdbc:mysql://localhost:3306/test_db
        return new ChainingMysqlDataSource()
            .setServerName("localhost")
            .setPort(3306)
            .setDatabaseName("test_db")
            .setUser("test_user")
            .setPassword("test_password")
            .getMysqlDataSource();
    }

    /** Start the test database in a separate thread. */
    public static void startTestDatabaseInThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(startTestDatabase);

        try {
            // wait for future to complete
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // await database startup
        DataSource dataSource = getTestDataSource();
        do {
            try {
                // sleep for 200 milliseconds
                Thread.sleep(200);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } while (!dataSourceIsValid(dataSource));

        // Find container ID
        future = executor.submit(findContainerID);
        try {
            // wait for future to complete
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // shutdown executor
        executor.shutdown();
    }

    /** Close the test database in a separate thread. */
    public static void closeTestDatabaseInThread() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(closeContainer);
        try {
            // wait for future to complete
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            // handle exception
        }

        // shutdown executor
        executor.shutdown();
    }
}
