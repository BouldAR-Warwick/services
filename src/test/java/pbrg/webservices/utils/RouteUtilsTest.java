package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static pbrg.webservices.database.AuthenticationController.addUser;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.GymController.addGym;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.RouteController.addImageToRoute;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.utils.RouteUtils.createRouteImagePython;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;
import static pbrg.webservices.utils.RouteUtils.getPythonScriptsDir;
import static pbrg.webservices.utils.RouteUtils.getRouteContentJSONArray;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileName;
import static pbrg.webservices.utils.RouteUtils.plotHoldsOnImagePython;
import static pbrg.webservices.utils.RouteUtils.setPythonScriptsDir;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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


    /** The example invalid route ID. */
    private static final int INVALID_ROUTE_ID = -1;

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
        String workingDirectory = ServletUtils.getWallImagePath();

        // when: plotting holds on image
        String newFile = plotHoldsOnImagePython(
            EXAMPLE_ROUTE_ID, wallImageFilePath,
            workingDirectory, workingDirectory,
            holds
        );

        // then: file is successfully created
        assertNotNull(newFile);

        // after: remove the file
        File file = new File(ServletUtils.getRouteImagePath(), newFile);
        assertTrue(file.delete());
    }

    @Test
    void generateRouteMoonBoardFileDoesNotExist() {
        // given: a file path that does not exist
        String originalPath = getPythonScriptsDir();
        setPythonScriptsDir("/dev/null/");

        assertThrows(
            // then: an IOException should be thrown
            IOException.class,

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
            // then: an IOException should be thrown
            IOException.class,

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

        // when: trying to create the route thumbnail
        String routeImageFileName = createRouteImagePython(routeId);

        // then: routeImageFileName should be null
        assertNull(routeImageFileName);
    }

    @Test
    void createRouteImagePythonEmptyHoldArray()
        throws NullPointerException {
        // given: a route that empty hold array
        String routeContent = "[]";
        Integer userId = addUser("test", "test", "test");
        assertNotNull(userId);
        Integer gymId = addGym("Test Gym", "Test City");
        assertNotNull(gymId);
        Integer wallId = addWall(gymId, "Test Wall", "MoonBoard2016.jpg");
        assertNotNull(wallId);
        Integer routeId = addRoute(
            routeContent, AVERAGE_GRADE, userId, wallId
        );
        assertNotNull(routeId);

        // when: trying to create the route thumbnail
        String routeImage = createRouteImagePython(routeId);

        // then: routeImage should be generated
        assertNotNull(routeImage);

        // after: remove image
        File file = new File(ServletUtils.getRouteImagePath(), routeImage);
        assertTrue(file.delete());

        // after: delete models
        RouteUtils.deleteRoute(routeId);
        assertFalse(routeExists(routeId));
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }

    @Test
    void getRouteContentJSONArrayWithInvalidRoute() {
        // given: a route ID that does not exist
        // when: trying to getRouteContentJSONArray
        JSONArray routeContent = getRouteContentJSONArray(INVALID_ROUTE_ID);

        // then: routeContent should be null
        assertNull(routeContent);
    }

    @Test
    void getRouteImageFileNameByRouteIdWithInvalidRoute() {
        // given: a route ID that does not exist
        // when: trying to getRouteImageFileNamesByRouteId
        String routeImageFileName =
            getRouteImageFileName(INVALID_ROUTE_ID);

        // then: routeImageFileName should be null
        assertNull(routeImageFileName);
    }

    @Test
    void getRouteImageFileNameByRouteIdValid() {
        // given: a route with a single hold
        String routeContent = "[{x: 0.570, y: 0.655}]";
        Integer userId = addUser("test", "test", "test");
        assertNotNull(userId);
        Integer gymId = addGym("Test Gym", "Test City");
        assertNotNull(gymId);
        Integer wallId = addWall(gymId, "Test Wall", "wall1.jpg");
        assertNotNull(wallId);
        Integer routeId = addRoute(
            routeContent, AVERAGE_GRADE, userId, wallId
        );
        assertNotNull(routeId);

        // generate the route image, add to route
        String routeImage = createRouteImagePython(routeId);
        assertNotNull(routeImage);
        assertTrue(addImageToRoute(routeId, routeImage));

        // when: getting the route image file name
        String routeImageFileName = getRouteImageFileName(routeId);

        // then: the file name is not null
        assertNotNull(routeImageFileName);

        // after: delete models
        RouteUtils.deleteRoute(routeId);
        assertFalse(routeExists(routeId));
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }
}
