package pbrg.webservices.servlets;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import static pbrg.webservices.database.GymController.addGym;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.RouteController.addImageToRoute;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.RouteControllerTest.createTestRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallController.wallExists;
import static pbrg.webservices.database.WallControllerTest.createTestWall;
import static pbrg.webservices.utils.RouteUtils.createAndStoreRouteImage;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileName;
import static pbrg.webservices.utils.ServletUtils.getContentType;
import static pbrg.webservices.utils.ServletUtils.getRouteImagePath;
import static pbrg.webservices.utils.ServletUtils.setRouteImagePath;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.database.RouteController;
import pbrg.webservices.utils.RouteUtils;

class GetRouteImageServletTest {

    /** An example route grade. */
    private static final int GRADE = 6;

    /** The user id of the test user. */
    private int userId;

    /** The gym id of the test gym. */
    private int gymId;

    /** The gym id of the test gym (with un-readable wall). */
    private int gymIdWithUnreadableWall;

    /** The wall id of the test wall. */
    private int wallId;

    /** The wall id of the test wall (cannot be returned). */
    private int wallIdUnreadable;

    /** The route id of the partial (image not gen) test route. */
    private int routeIdPartial;

    /** The route id of the full (image gen) test route. */
    private int routeIdFull;

    /** The route id of the full (image gen) (un-readable) test route. */
    private int routeIdFullUnreadable;

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
    void createModels() throws IOException {
        deleteModels();
        userId = createTestUser();

        // create the gyms
        gymId = createTestGym();
        Integer newGym =
            addGym("Test gym with an un-returnable wall", "Test Gym");
        assertNotNull(newGym);
        gymIdWithUnreadableWall = newGym;

        // create the walls
        wallId = createTestWall(gymId);
        wallIdUnreadable = createTestWall(gymIdWithUnreadableWall);

        // create the routes
        routeIdPartial = createTestRoute(userId, wallId);

        // create the full route
        JSONArray route = generateRouteMoonBoard(GRADE);
        assertNotNull(route);
        Integer newRoute = addRoute(route.toString(), GRADE, userId, wallId);
        assertNotNull(newRoute);
        assertTrue(createAndStoreRouteImage(newRoute));
        routeIdFull = newRoute;

        // create the full route (un-readable)
        newRoute = addRoute(route.toString(), GRADE, userId, wallIdUnreadable);
        assertNotNull(newRoute);
        addImageToRoute(newRoute, "MoonBoard2016.heic");
        routeIdFullUnreadable = newRoute;

        assertTrue(routeExists(routeIdPartial));
        assertTrue(routeExists(routeIdFull));
        assertTrue(routeExists(routeIdFullUnreadable));
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets

        // delete routes
        if (routeExists(routeIdFullUnreadable)) {
            // do not delete the route as used in testing
            RouteController.deleteRoute(routeIdFullUnreadable);
            assertFalse(routeExists(routeIdFullUnreadable));
        }
        if (routeExists(routeIdFull)) {
            RouteUtils.deleteRoute(routeIdFull);
            assertFalse(routeExists(routeIdFull));
        }
        if (routeExists(routeIdPartial)) {
            RouteUtils.deleteRoute(routeIdPartial);
            assertFalse(routeExists(routeIdPartial));
        }

        // delete walls
        if (wallExists(wallIdUnreadable)) {
            assertTrue(deleteWall(wallIdUnreadable));
            assertFalse(wallExists(wallIdUnreadable));
        }
        if (wallExists(wallId)) {
            assertTrue(deleteWall(wallId));
            assertFalse(wallExists(wallId));
        }

        // delete gyms
        if (gymExists(gymIdWithUnreadableWall)) {
            assertTrue(deleteGym(gymIdWithUnreadableWall));
            assertFalse(gymExists(gymIdWithUnreadableWall));
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
        GetRouteImageServlet servlet = spy(new GetRouteImageServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingInvalidRouteId() throws IOException {
        // given an invalid route id
        int invalidRouteId = -1;

        // mock the session
        String routeIdKey = "rid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(invalidRouteId);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetRouteImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingRouteWithoutImage() throws IOException {
        // given a route without an image (not generated)
        // mock the session
        String routeIdKey = "rid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(routeIdPartial);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetRouteImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );
    }

    @Test
    void failingRouteImageNotFound() throws IOException {
        // given a route with an image that does not exist
        String originalRouteImagePath = getRouteImagePath();
        setRouteImagePath("/dev/null/not/a/real/path");

        // mock the session
        String routeIdKey = "rid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(routeIdFull);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetRouteImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after: reset the route image path
        setRouteImagePath(originalRouteImagePath);
    }

    @Test
    void failingRouteImageNotReturned() throws IOException {
        // given a route with an image that cannot be returned
        // mock the session
        String routeIdKey = "rid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey))
            .thenReturn(routeIdFullUnreadable);
        String routeImageFileName =
            getRouteImageFileName(routeIdFullUnreadable);
        assertNull(getContentType(getExtension(routeImageFileName)));

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(response.getOutputStream())
            .thenReturn(mock(ServletOutputStream.class));

        // when getting the wall image
        new GetRouteImageServlet().doPost(request, response);

        // then: ensure the error is SC_INTERNAL_SERVER_ERROR
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );
    }

    @Test
    void passing() throws IOException {
        // given a route with an image that can be returned
        // mock the session
        String routeIdKey = "rid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(routeIdKey)).thenReturn(routeIdFull);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(response.getOutputStream())
            .thenReturn(mock(ServletOutputStream.class));

        // when getting the wall image
        new GetRouteImageServlet().doPost(request, response);

        // then ensure success
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}
