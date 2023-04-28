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
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.AuthenticationControllerTest.createTestUser;
import static pbrg.webservices.database.GymController.addGym;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.RouteController.userOwnsRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallController.getWallIdFromRoute;
import static pbrg.webservices.database.WallController.wallExists;
import static pbrg.webservices.database.WallControllerTest.createTestWall;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.RouteUtils;

class GenerateRouteServletTest {

    /** The average Hueco grade. */
    private static final int AVERAGE_GRADE = 6;

    /** The test user id. */
    private static int uid;

    /** The test gym id. */
    private static int gid;

    /** The test wall id. */
    private static int wid;

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

    @BeforeEach
    void constructDatabaseModels() {
        uid = createTestUser();
        gid = createTestGym();
        wid = createTestWall(gid);
    }

    @AfterEach
    void deleteDatabaseModels() {
        if (wallExists(wid)) {
            assertTrue(deleteWall(wid));
            assertFalse(wallExists(wid));
        }
        if (gymExists(gid)) {
            assertTrue(deleteGym(gid));
            assertFalse(gymExists(gid));
        }
        if (userExists(uid)) {
            assertTrue(deleteUser(uid));
            assertFalse(userExists(uid));
        }
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mockRequestWithBody("");
        HttpServletResponse response = mock(HttpServletResponse.class);
        GenerateRouteServlet servlet = spy(new GenerateRouteServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void validRunThrough() throws IOException {
        // given: a user, a gym with a wall, a grade

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gid);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then: verify the response contains the route id

        // capture the content written to the writer
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(captor.capture());
        JSONObject content = new JSONObject(captor.getValue());

        assertTrue(content.has("routeId"));
        int routeId = content.getInt("routeId");
        assertTrue(userOwnsRoute(uid, routeId));

        verify(response).setStatus(HttpServletResponse.SC_OK);

        // after: remove route
        RouteUtils.deleteRoute(routeId);
    }

    @Test
    void failingInvalidUser() throws IOException {
        // given an invalid user
        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(-1);
        when(session.getAttribute("gid")).thenReturn(gid);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );
    }

    @Test
    void failingInvalidGym() throws IOException {
        // given an invalid gym
        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(-1);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );
    }

    @Test
    void gymWithoutWallToBeMocked() throws IOException {
        // given a gym without a wall
        Integer gidWithoutWall = addGym("Gym without wall", "Test Location");
        assertNotNull(gidWithoutWall);

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gidWithoutWall);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then: verify the response contains the route id

        // capture the content written to the writer
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).write(captor.capture());
        JSONObject content = new JSONObject(captor.getValue());

        assertTrue(content.has("routeId"));
        int routeId = content.getInt("routeId");
        assertTrue(userOwnsRoute(uid, routeId));

        verify(response).setStatus(HttpServletResponse.SC_OK);

        // get wall from route
        Integer wallId = getWallIdFromRoute(routeId);
        assertNotNull(wallId);

        // after: remove route, wall
        RouteUtils.deleteRoute(routeId);
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gidWithoutWall));
    }

    @Test
    void failingRouteThumbnailGenerationFails() throws IOException {
        // given route generation fails
        String originalRouteGenScript = RouteUtils.getRouteGenerationScript();
        RouteUtils.setRouteGenerationScript("not a file");

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gid);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );

        // reset the route generation script
        RouteUtils.setRouteGenerationScript(originalRouteGenScript);
    }

    @Test
    void failingHoldPlottingFails() throws IOException {
        // given route generation fails
        String originalHoldPlottingScript = RouteUtils.getHoldPlottingScript();
        RouteUtils.setHoldPlottingScript("not a file");

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gid);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );

        // reset the hold plotting script
        RouteUtils.setHoldPlottingScript(originalHoldPlottingScript);
    }

    @Test
    void failingRouteThumbnailGenerationFailsMockedWall() throws IOException {
        // given route generation fails
        String originalRouteGenScript = RouteUtils.getRouteGenerationScript();
        RouteUtils.setRouteGenerationScript("not a file");

        // given a gym without a wall
        Integer gidWithoutWall = addGym("Gym without wall", "Test Location");
        assertNotNull(gidWithoutWall);

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gidWithoutWall);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );

        // reset the route generation script
        RouteUtils.setRouteGenerationScript(originalRouteGenScript);

        // after: remove gym
        assertTrue(deleteGym(gidWithoutWall));
    }

    @Test
    void failingHoldPlottingFailsMockedWall() throws IOException {
        // given route generation fails
        String originalHoldPlottingScript = RouteUtils.getHoldPlottingScript();
        RouteUtils.setHoldPlottingScript("not a file");

        // given a gym without a wall
        Integer gidWithoutWall = addGym("Gym without wall", "Test Location");
        assertNotNull(gidWithoutWall);

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gidWithoutWall);

        // mock request
        String bodyString = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // then ensure error is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );

        // reset the hold plotting script
        RouteUtils.setHoldPlottingScript(originalHoldPlottingScript);

        // after: remove route
        assertTrue(deleteGym(gidWithoutWall));
    }
}
