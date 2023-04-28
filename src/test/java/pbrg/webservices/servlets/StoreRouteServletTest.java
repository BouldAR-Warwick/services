package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.AuthenticationController.addUser;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.AuthenticationControllerTest.createTestUser;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.RouteControllerTest.createTestRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallController.wallExists;
import static pbrg.webservices.database.WallControllerTest.createTestWall;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.RouteUtils;

class StoreRouteServletTest {

    /** The test user id. */
    private static int userId;

    /** The test gym id. */
    private static int gymId;

    /** The test wall id. */
    private static int wallId;

    /** The test route id. */
    private static int routeId;

    @BeforeAll
    public static void startResources() throws IllegalStateException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @BeforeEach
    void createModels() {
        deleteModels();
        userId = createTestUser();
        gymId = createTestGym();
        wallId = createTestWall(gymId);
        routeId = createTestRoute(userId, wallId);
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
        if (routeExists(routeId)) {
            RouteUtils.deleteRoute(routeId);
            assertFalse(routeExists(routeId));
        }
        if (wallExists(wallId)) {
            assertTrue(deleteWall(wallId));
            assertFalse(wallExists(wallId));
        }
        if (gymExists(gymId)) {
            assertTrue(deleteGym(gymId));
            assertFalse(gymExists(gymId));
        }
        if (userExists(userId)) {
            assertTrue(deleteUser(userId));
            assertFalse(userExists(userId));
        }
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StoreRouteServlet servlet = spy(new StoreRouteServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingInvalidUser() throws IOException {
        // given the user is invalid
        String userIdKey = "uid";
        String routeIdKey = "routeID";
        int invalidUserId = -1;

        // build session storing user id
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(invalidUserId);

        // build the request storing the route id; the response
        String bodyString = "{\"" + routeIdKey + "\": " + routeId + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when storing the route
        new StoreRouteServlet().doPost(request, response);

        // then SC_BAD_REQUEST is returned
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingInvalidRoute() throws IOException {
        // given the route is invalid
        String userIdKey = "uid";
        String routeIdKey = "routeID";
        int invalidRouteId = -1;

        // build session storing user id
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(userId);

        // build the request storing the route id; the response
        String bodyString = "{\"" + routeIdKey + "\": " + invalidRouteId + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when storing the route
        new StoreRouteServlet().doPost(request, response);

        // then SC_BAD_REQUEST is returned
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingUserDoesNotOwnRoute() throws IOException {
        // given the user does not own the route
        Integer otherUserId = addUser(
            "other username", "other email", "other password"
        );
        assertNotNull(otherUserId);
        int otherRouteId = createTestRoute(otherUserId, wallId);

        String userIdKey = "uid";
        String routeIdKey = "routeID";

        // build session storing user id
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(userId);

        // build the request storing the route id; the response
        String bodyString = "{\"" + routeIdKey + "\": " + otherRouteId + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when storing the route
        new StoreRouteServlet().doPost(request, response);

        // then SC_EXPECTATION_FAILED is returned
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after: remove the database assets
        RouteUtils.deleteRoute(otherRouteId);
        assertFalse(routeExists(otherRouteId));
        assertTrue(deleteUser(otherUserId));
        assertFalse(userExists(otherUserId));
    }

    @Test
    void passing() throws IOException {
        // given a user who owns a route
        String userIdKey = "uid";
        String routeIdKey = "routeID";

        // build session storing user id
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(userId);

        // build the request storing the route id; the response
        String bodyString = "{\"" + routeIdKey + "\": " + routeId + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when storing the route
        new StoreRouteServlet().doPost(request, response);

        // verify successful response
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}
