package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.DatabaseTestMethods.mockConnectionThrowsException;
import static pbrg.webservices.database.DatabaseTestMethods.mockEmptyResultSet;
import static pbrg.webservices.database.DatabaseTestMethods.mockNoAffectedRows;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.RouteController.getRoutesInGymMadeByUser;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.Route;

final class RouteControllerTest {

    /** The first route id in the test database. */
    private static final int FIRST_ROUTE_ID = 10;

    /** The first route difficulty in the test database. */
    private static final int FIRST_ROUTE_DIFFICULTY = 3;

    /** The second route id in the test database. */
    private static final int SECOND_ROUTE_ID = 20;

    /** The second route difficulty in the test database. */
    private static final int SECOND_ROUTE_DIFFICULTY = 4;


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
        Constructor<RouteController> constructor;
        try {
            constructor = RouteController.class.getDeclaredConstructor();
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
    void userOwnsRouteEmptyResults() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when checking if user owns route
        boolean userOwnsRoute = RouteController.userOwnsRoute(-1, -1);

        // then: user should not own route
        assertFalse(userOwnsRoute);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void addRouteEmptyResults() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockEmptyResultSet();
        DatabaseController.setDataSource(mockDataSource);

        // when adding a route with empty results
        Integer routeId = addRoute("[]", -1, -1, -1);

        // then: routeId should be null
        assertNull(routeId);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void addImageToRouteNoAffectedRows() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockNoAffectedRows();
        DatabaseController.setDataSource(mockDataSource);

        // when adding an image to a route with no rows effected
        boolean imageAdded = RouteController.addImageToRoute(-1, "");

        // then: image should not be added
        assertFalse(imageAdded);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteRouteByCreatorAndWallNoAffectedRows() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockNoAffectedRows();
        DatabaseController.setDataSource(mockDataSource);

        // when deleting a route with no rows effected
        boolean routeDeleted = RouteController.deleteRoute(-1, -1);

        // then: route should not be deleted
        assertFalse(routeDeleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteRouteNoAffectedRows() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockNoAffectedRows();
        DatabaseController.setDataSource(mockDataSource);

        // when deleting a route with no rows effected
        boolean routeDeleted = RouteController.deleteRoute(-1);

        // then: route should not be deleted
        assertFalse(routeDeleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteRouteByCreatorAndWallConnectionThrowsException()
        throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockConnectionThrowsException();
        DatabaseController.setDataSource(mockDataSource);

        // when deleting a route with no rows effected
        boolean routeDeleted = RouteController.deleteRoute(-1, -1);

        // then: route should not be deleted
        assertFalse(routeDeleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void deleteRouteConnectionThrowsException() throws SQLException {
        // inject the mock data source
        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockConnectionThrowsException();
        DatabaseController.setDataSource(mockDataSource);

        // when deleting a route with no rows effected
        boolean routeDeleted = RouteController.deleteRoute(-1);

        // then: route should not be deleted
        assertFalse(routeDeleted);

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRoutesInGymMadeByUserReturnsEmptyListWhenNoRoutesFound()
        throws SQLException {
        // Arrange
        int gymId = 1;
        int userId = 2;
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false); // no routes found

        // Act
        List<Route> routes = getRoutesInGymMadeByUser(gymId, userId);

        // Assert
        assertTrue(routes.isEmpty());
    }

    @Test
    void getRoutesInGymMadeByUserReturnsListOfRoutesWhenRoutesFound()
        throws SQLException {
        // Arrange
        int gymId = 1;
        int userId = 2;
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement pst = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(pst);
        when(pst.executeQuery()).thenReturn(rs);

        // simulate two routes found
        when(rs.next())
            .thenReturn(true).thenReturn(true).thenReturn(false);
        when(rs.getInt("RID"))
            .thenReturn(FIRST_ROUTE_ID).thenReturn(FIRST_ROUTE_ID)
            .thenReturn(SECOND_ROUTE_ID).thenReturn(SECOND_ROUTE_ID);
        when(rs.getInt("Difficulty"))
            .thenReturn(FIRST_ROUTE_DIFFICULTY)
            .thenReturn(SECOND_ROUTE_DIFFICULTY);

        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(dataSource);

        // Act
        List<Route> routes = getRoutesInGymMadeByUser(gymId, userId);

        // Assert
        assertEquals(2, routes.size());

        // check first route
        Route route1 = routes.get(0);
        assertEquals(FIRST_ROUTE_ID, route1.getRouteId());
        assertEquals(FIRST_ROUTE_DIFFICULTY, route1.getDifficulty());
        assertEquals("Route #" + FIRST_ROUTE_ID, route1.getRouteName());

        // check second route
        Route route2 = routes.get(1);
        assertEquals(SECOND_ROUTE_ID, route2.getRouteId());
        assertEquals(SECOND_ROUTE_DIFFICULTY, route2.getDifficulty());
        assertEquals("Route #" + SECOND_ROUTE_ID, route2.getRouteName());

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRoutesInGymMadeByUserThrowsSQLExceptionWhenConnectionFails()
        throws SQLException {
        // Arrange
        int gymId = 1;
        int userId = 2;

        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockConnectionThrowsException();
        DatabaseController.setDataSource(mockDataSource);

        assertThrows(
            // then: SQLException should be thrown
            SQLException.class,

            // when: getting routes
            () -> getRoutesInGymMadeByUser(gymId, userId)
        );

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRoutesInGymMadeByUserEmptyResultSet() throws SQLException {
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when getting routes
        List<Route> routes = getRoutesInGymMadeByUser(1, 1);

        // then: routes should be empty
        assertTrue(routes.isEmpty());

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }
}
