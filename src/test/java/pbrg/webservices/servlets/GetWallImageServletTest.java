package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallController.wallExists;
import static pbrg.webservices.database.WallControllerTest.createTestWall;

import jakarta.servlet.ServletOutputStream;
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
import pbrg.webservices.utils.ServletUtils;

class GetWallImageServletTest {

    /** The user id of the test user. */
    private int userId;

    /** The gym id of the test gym (with wall). */
    private int gymIdWithWall;

    /** The gym id of the test gym (without wall). */
    private int gymIdWithoutWall;

    /** The gym id of the test gym (with un-readable wall). */
    private int gymIdWithUnreadableWall;

    /** The wall id of the test wall (can be returned). */
    private int wallIdReadable;

    /** The wall id of the test wall (cannot be returned). */
    private int wallIdUnreadable;

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

        // the gyms
        gymIdWithWall = createTestGym();
        Integer newGym =
            // create a gym without a wall
            addGym("Test gym without a wall", "Test Gym 2");
        assertNotNull(newGym);
        gymIdWithoutWall = newGym;
        newGym = addGym("Test gym with an un-returnable wall", "Test Gym");
        assertNotNull(newGym);
        gymIdWithUnreadableWall = newGym;
        assertTrue(gymExists(gymIdWithWall));
        assertTrue(gymExists(gymIdWithoutWall));
        assertTrue(gymExists(gymIdWithUnreadableWall));

        // the walls
        wallIdReadable = createTestWall(gymIdWithWall);
        // create a wall with an un-returnable image (wrong extension)
        Integer newWall = addWall(
            gymIdWithUnreadableWall, "Test wall", "MoonBoard2016.heic"
        );
        assertNotNull(newWall);
        wallIdUnreadable = newWall;
        assertTrue(wallExists(wallIdReadable));
        assertTrue(wallExists(wallIdUnreadable));
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
        if (wallExists(wallIdUnreadable)) {
            assertTrue(deleteWall(wallIdUnreadable));
            assertFalse(wallExists(wallIdUnreadable));
        }
        if (wallExists(wallIdReadable)) {
            assertTrue(deleteWall(wallIdReadable));
            assertFalse(wallExists(wallIdReadable));
        }
        if (gymExists(gymIdWithUnreadableWall)) {
            assertTrue(deleteGym(gymIdWithUnreadableWall));
            assertFalse(gymExists(gymIdWithUnreadableWall));
        }
        if (gymExists(gymIdWithWall)) {
            assertTrue(deleteGym(gymIdWithWall));
            assertFalse(gymExists(gymIdWithWall));
        }
        if (gymExists(gymIdWithoutWall)) {
            assertTrue(deleteGym(gymIdWithoutWall));
            assertFalse(gymExists(gymIdWithoutWall));
        }
        if (userExists(userId)) {
            assertTrue(deleteUser(userId));
            assertFalse(userExists(userId));
        }
    }

    /** The servlet to test. */
    private static final GetWallImageServlet SERVLET =
        new GetWallImageServlet();

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        GetWallImageServlet servlet = spy(new GetWallImageServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void doPostWithNulls() {
        assertThrows(
            // then: throws Exception
            Exception.class,

            // when: requested with null request and response
            () -> SERVLET.doPost(null, null)
        );
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void emptyResponse() {
        // given: valid request, null response
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(
            // then: throws Exception
            Exception.class,

            // when: requested with null request and response
            () -> SERVLET.doPost(request, null)
        );

        // then: ensure the session is never called
        verify(request, never()).getSession();
    }

    @Test
    void requestWithoutSession() throws IOException {
        // given: valid response, request with no session
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // Set up mock behaviour
        when(request.getSession()).thenReturn(null);

        // Call the method to test
        SERVLET.doPost(request, response);

        // check response has HttpServletResponse.SC_UNAUTHORIZED
        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED),
            anyString()
        );
    }

    @Test
    void failingInvalidGym() throws IOException {
        // given a gym that does not exist
        int invalidGymId = -1;

        // mock the session
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(gymIdKey)).thenReturn(invalidGymId);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetWallImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingGymWithoutWall() throws IOException {
        // given a gym that does not have a wall
        // mock the session
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(gymIdKey)).thenReturn(gymIdWithoutWall);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetWallImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingWallImageFileNotFound() throws IOException {
        // given a gym that has a wall but no local image
        String originalWallImagePath = ServletUtils.getWallImagePath();
        ServletUtils.setWallImagePath("/dev/null/does/not/exist");

        // mock the session
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(gymIdKey)).thenReturn(gymIdWithWall);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetWallImageServlet().doPost(request, response);

        // then: ensure the error is HttpServletResponse.SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after test: restore the original wall image path
        ServletUtils.setWallImagePath(originalWallImagePath);
    }

    @Test
    void failingWallImageNotReturned() throws IOException {
        // given a gym that has a wall and a local image, but return fails
        // mock the session
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(gymIdKey))
            .thenReturn(gymIdWithUnreadableWall);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when getting the wall image
        new GetWallImageServlet().doPost(request, response);

        // then: ensure the error is SC_INTERNAL_SERVER_ERROR
        verify(response).sendError(
            eq(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
            anyString()
        );
    }

    @Test
    void passing() throws IOException {
        // given a gym that has a wall and a local image
        // mock the session
        String gymIdKey = "gid";
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute(gymIdKey)).thenReturn(gymIdWithWall);

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(response.getOutputStream())
            .thenReturn(mock(ServletOutputStream.class));

        // when getting the wall image
        new GetWallImageServlet().doPost(request, response);

        // then ensure the response is HttpServletResponse.SC_OK
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}
