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
import static pbrg.webservices.database.GymController.removeUserPrimaryGym;
import static pbrg.webservices.database.GymController.setPrimaryGym;
import static pbrg.webservices.database.GymController.userHasPrimaryGym;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;

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

class GetPrimaryGymServletTest {

    /** The user id of the test user. */
    private int userId;

    /** The gym id of the test gym. */
    private int gymId;

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
        assertTrue(userExists(userId));

        gymId = createTestGym();
        assertTrue(gymExists(gymId));
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
        if (gymExists(gymId)) {
            assertTrue(deleteGym(gymId));
        }
        if (userExists(userId)) {
            assertTrue(deleteUser(userId));
        }
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        GetPrimaryGymServlet servlet = spy(new GetPrimaryGymServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void testDoPostNoSession() throws IOException {
        // given a request without a session
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);

        // when getting the primary gym of a user id
        new GetPrimaryGymServlet().doPost(request, response);

        // then the response should be unauthorized
        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED),
            anyString()
        );
    }

    @Test
    void testDoPostNoUserId() throws IOException {
        // given a request without a user id
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(null);

        // when querying the primary gym of the user id
        new GetPrimaryGymServlet().doPost(request, response);

        // then the response should be unauthorized
        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED),
            anyString()
        );
    }

    @Test
    void testDoPostInvalidUserId() throws IOException {
        // given a request with an invalid user id
        int invalidUserId = -1;

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(invalidUserId);

        // when querying the primary gym of the user id
        new GetPrimaryGymServlet().doPost(request, response);

        // then the response should be unauthorized
        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED),
            anyString()
        );
    }

    @Test
    void testDoPostNotPrimaryGym() throws IOException {
        // given user, gym where gym is not the user's primary gym
        assertFalse(userHasPrimaryGym(userId));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(userId);

        // mock a response writer
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when querying the primary gym of the user id
        new GetPrimaryGymServlet().doPost(request, response);

        // then ensure a successful empty response
        verify(response).getWriter();
        verify(writer).write("{}");
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testDoPostUserHasPrimaryGym() throws IOException {
        // given user, gym where gym is not the user's primary gym
        assertFalse(userHasPrimaryGym(userId));
        assertTrue(setPrimaryGym(userId, gymId));
        assertTrue(userHasPrimaryGym(userId));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(anyBoolean())).thenReturn(session);
        when(session.getAttribute("uid")).thenReturn(userId);

        // mock a response writer
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);

        // when querying the primary gym of the user id
        new GetPrimaryGymServlet().doPost(request, response);

        // then ensure a successful empty response
        verify(response).getWriter();
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // after: remove the database assets
        assertTrue(removeUserPrimaryGym(userId));
        assertFalse(userHasPrimaryGym(userId));
    }
}