package pbrg.webservices.database;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnGetConnection;
import static pbrg.webservices.database.DatabaseTestMethods.mockEmptyResultSet;
import static pbrg.webservices.database.DatabaseTestMethods.mockNoAffectedRows;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.RouteController.getRouteByRouteId;
import static pbrg.webservices.database.RouteController.getRouteContent;
import static pbrg.webservices.database.RouteController.getRoutesInGymMadeByUser;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.wallExists;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pbrg.webservices.models.Route;
import pbrg.webservices.models.RouteFull;

public final class RouteControllerTest {

    /** The test route difficulty. */
    private static final int TEST_ROUTE_DIFFICULTY = 6;

    /** The test route content. */
    private static final String TEST_ROUTE_CONTENT = "[{x: 0.5, y: 0.5}]";

    /** The first route id in the test database. */
    private static final int FIRST_ROUTE_CREATOR_ID = 10;

    /** The first route difficulty in the test database. */
    private static final int FIRST_ROUTE_DIFFICULTY = 3;

    /** The second route id in the test database. */
    private static final int SECOND_ROUTE_CREATOR_ID = 20;

    /** The second route difficulty in the test database. */
    private static final int SECOND_ROUTE_DIFFICULTY = 4;

    /**
     * Create the test route.
     * @param userId the creator user id
     * @param wallId the wall id
     * @return the route id
     */
    public static int createTestRoute(final int userId, final int wallId) {
        // ensure user exists
        assertTrue(userExists(userId));

        // ensure wall exists
        assertTrue(wallExists(wallId));

        // create the route
        Integer routeId = addRoute(
            TEST_ROUTE_CONTENT, TEST_ROUTE_DIFFICULTY,
            userId, wallId
        );

        // ensure route was created
        assertNotNull(routeId);
        assertTrue(routeExists(routeId));
        return routeId;
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
    void userOwnsRouteEmptyResults() {
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
    void userOwnsRouteThrowing() {
        // inject the mock data source
        DatabaseController.setDataSource(
            mockThrowsExceptionOnGetConnection()
        );

        // when checking if user owns route
        boolean userOwnsRoute = RouteController.userOwnsRoute(-1, -1);

        // then: user should not own route
        assertFalse(userOwnsRoute);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void addRouteEmptyResults() {
        // inject the mock data source
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when adding a route with empty results
        Integer routeId = addRoute("[]", -1, -1, -1);

        // then: routeId should be null
        assertNull(routeId);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void addRouteThrowing() {
        // inject the mock data source
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());

        // when adding a route with empty results
        Integer routeId = addRoute("[]", -1, -1, -1);

        // then: routeId should be null
        assertNull(routeId);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void addImageToRouteNoAffectedRows() {
        // inject the mock data source
        DatabaseController.setDataSource(mockNoAffectedRows());

        // when adding an image to a route with no rows effected
        boolean imageAdded = RouteController.addImageToRoute(-1, "");

        // then: image should not be added
        assertFalse(imageAdded);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void addImageToRouteThrowsException() {
        // inject the mock data source
        DatabaseController.setDataSource(
            mockThrowsExceptionOnGetConnection()
        );

        // when adding an image to a route with no rows effected
        boolean imageAdded = RouteController.addImageToRoute(-1, "");

        // then: image should not be added
        assertFalse(imageAdded);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Nested
    class DeleteRoute {
        @Test
        void deleteRouteNoAffectedRows() {
            // inject the mock data source
            DatabaseController.setDataSource(mockNoAffectedRows());
            int invalidRouteId = -1;

            // when deleting a route with no rows effected
            boolean routeDeleted = RouteController.deleteRoute(invalidRouteId);

            // then: route should not be deleted
            assertFalse(routeDeleted);

            // after: restore original data source
            DatabaseController.setDataSource(getTestDataSource());
        }

        @Test
        void deleteRouteConnectionThrowsException() {
            // inject the mock data source
            DataSource originalDataSource = DatabaseController.getDataSource();
            DataSource mockDataSource = mockThrowsExceptionOnGetConnection();
            DatabaseController.setDataSource(mockDataSource);

            // when deleting a route with no rows effected
            boolean routeDeleted = RouteController.deleteRoute(-1);

            // then: route should not be deleted
            assertFalse(routeDeleted);

            // after: restore original data source
            DatabaseController.setDataSource(originalDataSource);
        }
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
            .thenReturn(FIRST_ROUTE_CREATOR_ID)
            .thenReturn(FIRST_ROUTE_CREATOR_ID)
            .thenReturn(SECOND_ROUTE_CREATOR_ID)
            .thenReturn(SECOND_ROUTE_CREATOR_ID);
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
        assertEquals(FIRST_ROUTE_CREATOR_ID, route1.getRouteId());
        assertEquals(FIRST_ROUTE_DIFFICULTY, route1.getDifficulty());
        assertEquals("Route #" + FIRST_ROUTE_CREATOR_ID, route1.getRouteName());

        // check second route
        Route route2 = routes.get(1);
        assertEquals(SECOND_ROUTE_CREATOR_ID, route2.getRouteId());
        assertEquals(SECOND_ROUTE_DIFFICULTY, route2.getDifficulty());
        assertEquals(
            "Route #" + SECOND_ROUTE_CREATOR_ID,
            route2.getRouteName()
        );

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRoutesInGymMadeByUserThrowsSQLExceptionWhenConnectionFails() {
        // Arrange
        int gymId = 1;
        int userId = 2;

        DataSource originalDataSource = DatabaseController.getDataSource();
        DataSource mockDataSource = mockThrowsExceptionOnGetConnection();
        DatabaseController.setDataSource(mockDataSource);

        // when: getting routes
        List<Route> routes = getRoutesInGymMadeByUser(gymId, userId);

        // then: routes should be empty
        assertTrue(routes.isEmpty());

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRoutesInGymMadeByUserEmptyResultSet() {
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when getting routes
        List<Route> routes = getRoutesInGymMadeByUser(1, 1);

        // then: routes should be empty
        assertTrue(routes.isEmpty());

        // after: restore original data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void getRouteContentThrowing() {
        // mock the data source
        int routeId = 1;
        DatabaseController.setDataSource(
            mockThrowsExceptionOnGetConnection()
        );

        // when: getting route content
        String routeContent = getRouteContent(routeId);

        // then: route content should be null
        assertNull(routeContent);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void getRouteContentEmptyResultSet() {
        // mock the data source
        int routeId = 1;
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: getting route content
        String routeContent = getRouteContent(routeId);

        // then: route content should be null
        assertNull(routeContent);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void routeExistsEmptyResultSet() {
        // mock the data source
        int routeId = 1;
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: checking if route exists
        boolean routeExists = routeExists(routeId);

        // then: route should not exist
        assertFalse(routeExists);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void getRouteByRouteIdEmptyResultSet() {
        // mock the data source
        int routeId = 1;
        DatabaseController.setDataSource(mockEmptyResultSet());

        // when: getting route by route id
        RouteFull route = getRouteByRouteId(routeId);

        // then: route should be null
        assertNull(route);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }

    @Test
    void getRouteByRouteIdThrowing() {
        // mock the data source
        int routeId = -1;
        DatabaseController.setDataSource(
            mockThrowsExceptionOnGetConnection()
        );

        // when: getting route by route id
        RouteFull route = getRouteByRouteId(routeId);

        // then: route should be null
        assertNull(route);

        // after: restore original data source
        DatabaseController.setDataSource(getTestDataSource());
    }
}
