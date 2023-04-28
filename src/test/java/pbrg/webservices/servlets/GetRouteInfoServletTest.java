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
import static pbrg.webservices.database.AuthenticationControllerTest
    .createTestUser;
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

class GetRouteInfoServletTest {

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
        GetRouteInfoServlet servlet = spy(new GetRouteInfoServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingInvalidRoute() throws IOException {
        // given an invalid route
        String routeIdKey = "rid";
        int invalidRouteId = -1;

        // mock the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(invalidRouteId);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the route info
        new GetRouteInfoServlet().doPost(request, response);

        // then SC_BAD_REQUEST is returned
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void passing() throws IOException {
        // given a valid route
        String routeIdKey = "rid";

        // mock the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(routeId);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when getting the route info
        new GetRouteInfoServlet().doPost(request, response);

        // then SC_OK is returned
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // and get writer is called
        verify(response).getWriter();
    }
}
