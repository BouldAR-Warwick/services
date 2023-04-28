package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.RouteUtils;

class GetRoutesServletTest {

    /** The user id of the test user. */
    private int userId;

    /** The gym id of the test gym. */
    private int gymId;

    /** The wall id of the test wall. */
    private int wallId;

    /** The route id of the test route. */
    private int routeId;

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
        assertTrue(routeExists(routeId));
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
        GetRoutesServlet servlet = spy(new GetRoutesServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingInvalidUser() throws IOException {
        // given an invalid user
        int invalidUserId = -1;

        // mock session
        String userIdKey = "uid";
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(invalidUserId);
        when(session.getAttribute(gymIdKey)).thenReturn(gymId);

        // mock request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the routes
        new GetRoutesServlet().doPost(request, response);

        // then ensure error is SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingInvalidGym() throws IOException {
        // given an invalid gym
        int invalidGymId = -1;

        // mock session
        String userIdKey = "uid";
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(userId);
        when(session.getAttribute(gymIdKey)).thenReturn(invalidGymId);

        // mock request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the routes
        new GetRoutesServlet().doPost(request, response);

        // then ensure error is SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void passing() throws IOException {
        // given a valid user and gym
        // mock session
        String userIdKey = "uid";
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(userIdKey)).thenReturn(userId);
        when(session.getAttribute(gymIdKey)).thenReturn(gymId);

        // mock request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when getting the routes
        new GetRoutesServlet().doPost(request, response);

        // then ensure success
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // and ensure the response is correct
        verify(response).getWriter();
    }
}
