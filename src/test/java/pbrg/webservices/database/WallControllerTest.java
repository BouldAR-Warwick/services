package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static pbrg.webservices.database.DatabaseTestMethods.mockConnectionThrowsException;
import static pbrg.webservices.database.DatabaseTestMethods.mockEmptyResultSet;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallController.getWallIdFromGymId;
import static pbrg.webservices.database.WallController.getWallIdFromRouteId;
import static pbrg.webservices.database.WallController.getWallImageFileName;
import static pbrg.webservices.database.WallController.getWallImageFileNameFromRouteId;
import static pbrg.webservices.database.WallController.gymHasWall;
import static pbrg.webservices.database.WallController.wallExists;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public final class WallControllerTest {

    /** The test wall content. */
    private static final String TEST_WALL_CONTENT = "[{x: 0.5, y: 0.5}]";

    /** The test wall image file name. */
    private static final String TEST_WALL_IMAGE_FILE_NAME = "MoonBoard2016.jpg";

    /**
     * Creates a test wall in the database.
     * @param gymId the gym id
     * @return the wall id
     */
    public static int createTestWall(final int gymId) {
        Integer wallId =
            addWall(gymId, TEST_WALL_CONTENT, TEST_WALL_IMAGE_FILE_NAME);
        assertNotNull(wallId);
        return wallId;
    }

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
        Constructor<WallController> constructor;
        try {
            constructor = WallController.class.getDeclaredConstructor();
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
    void addWallEmptyResults() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: adding the wall
        Integer wallId = addWall(-1, "", "");
        boolean added = wallId != null;

        // then: wall should not be added
        assertFalse(added);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getWallImageFileNameFromWallIdEmptyResults() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: getting the wall image file name from the wallId
        String fileName = getWallImageFileName(-1);

        // then: file name should be null
        assertNull(fileName);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getWallIdFromRouteIdTestNonExistentRoute() {
        // given an invalid routeId
        int routeId = -1;

        // when: get wallId from routeId
        Integer wallId = getWallIdFromRouteId(routeId);

        // then: wall should be null
        assertNull(wallId);
    }

    @Test
    void getWallIdFromGymIdEmptyResults() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: getting the wallId from the gymId
        Integer wallId = getWallIdFromGymId(-1);

        // then: wallId should be null
        assertNull(wallId);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void gymHasWallEmptyResults() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: checking if the gym has a wall
        boolean hasWall = gymHasWall(-1);

        // then: wall should not exist
        assertFalse(hasWall);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteWallEmptyResults() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: deleting the wall
        boolean deleted = deleteWall(-1);

        // then: wall should not be deleted
        assertFalse(deleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void addWallThrowsException() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: adding the wall
        Integer wallId = addWall(-1, "", "");
        boolean added = wallId != null;

        // then: wall should not be added
        assertFalse(added);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getWallIdFromRouteIdThrowsException() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: getting the wallId from the routeId
        Integer wallId = getWallIdFromRouteId(-1);

        // then: wallId should be null
        assertNull(wallId);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void gymHasWallThrowsException() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: checking if the gym has a wall
        boolean hasWall = gymHasWall(-1);

        // then: wall should not exist
        assertFalse(hasWall);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteWallThrowsException() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: deleting the wall
        boolean deleted = deleteWall(-1);

        // then: wall should not be deleted
        assertFalse(deleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getWallImageFileNameFromRouteIdFailingInvalidRoute() {
        // given an invalid routeId
        int routeId = -1;

        // when: get wall image file name from routeId
        String fileName = getWallImageFileNameFromRouteId(routeId);

        // then: file name should be null
        assertNull(fileName);
    }

    @Test
    void getWallImageFileNameThrowing() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: get wall image file name from routeId
        String fileName = getWallImageFileNameFromRouteId(-1);

        // then: file name should be null
        assertNull(fileName);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getWallIdFromGymIdThrowing() {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        // when: get wallId from gymId
        Integer wallId = getWallIdFromGymId(-1);

        // then: wallId should be null
        assertNull(wallId);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void wallExistsInvalidWallId() {
        // given an invalid wallId
        int wallId = 10;
        assertFalse(wallExists(wallId));

        // when: checking if the wall exists
        boolean exists = wallExists(wallId);

        // then: wall should not exist
        assertFalse(exists);
    }
}
