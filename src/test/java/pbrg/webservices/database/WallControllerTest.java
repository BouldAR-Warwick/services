package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static pbrg.webservices.database.WallController.getWallImageFileNameFromWallId;
import static pbrg.webservices.database.WallController.gymHasWall;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class WallControllerTest {

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
    void addWallEmptyResults() throws SQLException {
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
    void getWallImageFileNameFromWallIdEmptyResults() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when: getting the wall image file name from the wallId
        String fileName = getWallImageFileNameFromWallId(-1);

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
    void getWallIdFromGymIdEmptyResults() throws SQLException {
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
    void gymHasWallEmptyResults() throws SQLException {
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
    void deleteWallEmptyResults() throws SQLException {
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
    void addWallThrowsException() throws SQLException {
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
    void getWallIdFromRouteIdThrowsException() throws SQLException {
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
    void gymHasWallThrowsException() throws SQLException {
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
    void deleteWallThrowsException() throws SQLException {
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
}
