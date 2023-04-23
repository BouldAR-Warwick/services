package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static pbrg.webservices.database.CredentialController.addUser;
import static pbrg.webservices.database.CredentialController.deleteUser;
import static pbrg.webservices.database.GymController.addGym;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.RouteController.deleteRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.utils.RouteUtils.createRouteImagePython;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;
import static pbrg.webservices.utils.RouteUtils.getPythonScriptsDir;
import static pbrg.webservices.utils.RouteUtils.plotHoldsOnImagePython;
import static pbrg.webservices.utils.RouteUtils.setPythonScriptsDir;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;

final class RouteUtilsTest {
    /** The average bouldering grade worldwide is V5. */
    private static final int AVERAGE_GRADE = 5;

    /** The example route ID. */
    private static final int EXAMPLE_ROUTE_ID = 500;

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
        Constructor<RouteUtils> constructor;
        try {
            constructor = RouteUtils.class.getDeclaredConstructor();
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
    void generateRouteMoonBoardTest() throws IOException {
        // given: grade

        // when: generate route
        JSONArray result = generateRouteMoonBoard(AVERAGE_GRADE);

        // then: result is not null
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertFalse(result.toString().isBlank());
    }

    @Test
    void plotHoldsOnImage() throws IOException {
        // given: holds, paths, route ID, working directory
        JSONArray holds = generateRouteMoonBoard(AVERAGE_GRADE);
        assertNotNull(holds);
        assertFalse(holds.toString().isBlank());
        String wallImageFilePath = "MoonBoard2016.jpg";
        String workingDirectory = System.getProperty("user.dir");

        // when: plotting holds on image
        String newFile = plotHoldsOnImagePython(
            EXAMPLE_ROUTE_ID, wallImageFilePath,
            workingDirectory, workingDirectory,
            holds
        );

        // then: file is successfully created
        assertNotNull(newFile);
    }

    @Test
    void generateRouteMoonBoardFileDoesNotExist() {
        // given: a file path that does not exist
        String originalPath = getPythonScriptsDir();
        setPythonScriptsDir("/dev/null/");

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the file is checked
            () -> generateRouteMoonBoard(1)
        );

        // after: reset the path
        setPythonScriptsDir(originalPath);
    }

    @Test
    void plotHoldsOnImagePythonFileDoesNotExist() {
        // given: a file path that does not exist
        String originalPath = getPythonScriptsDir();
        String fakePath = "/dev/null/";
        setPythonScriptsDir(fakePath);

        File pythonFile = new File(
            fakePath,
            "plot-holds.py"
        );
        assertFalse(pythonFile.exists());

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the file is checked
            () -> plotHoldsOnImagePython(
                -1, "", "", "",
                mock(JSONArray.class)
            )
        );

        // after: reset the path
        setPythonScriptsDir(originalPath);
    }

    @Test
    void createRouteImagePythonNullWallFile() {
        // given: a route that has no wall image
        int routeId = -1;

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: trying to createRouteImagePython
            () -> createRouteImagePython(routeId)
        );
    }

    @Test
    void createRouteImagePythonEmptyHoldArray()
        throws SQLException, NullPointerException {
        // given: a route that empty hold array
        String routeContent = "[]";
        Integer userId = addUser("test", "test", "test");
        assertNotNull(userId);
        Integer gymId = addGym("Test Gym", "Test City");
        assertNotNull(gymId);
        Integer wallId = addWall(gymId, "Test Wall", "MoonBoard2016.jpg");
        assertNotNull(wallId);
        Integer routeId = addRoute(routeContent, AVERAGE_GRADE, userId, wallId);
        assertNotNull(routeId);

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: trying to createRouteImagePython
            () -> createRouteImagePython(routeId)
        );

        // after: delete models
        assertTrue(deleteRoute(routeId));
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }
}