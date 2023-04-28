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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;

class StoreGymServletTest {

    /** The test user id. */
    private static int userId;

    /** The test gym id. */
    private static int gymId;

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
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
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
        StoreGymServlet servlet = spy(new StoreGymServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingInvalidUser() throws IOException {
        // given a user with no primary gym

        // embed the user id in the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(-1);

        // mock the request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when calling the servlet
        new StoreGymServlet().doPost(request, response);

        // then ensure error is SC_BAD_REQUEST
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void failingUserHasNoPrimaryGym() throws IOException {
        // given a user with no primary gym

        // embed the user id in the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(userId);

        // mock the request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when calling the servlet
        new StoreGymServlet().doPost(request, response);

        // then ensure error is SC_NOT_FOUND
        verify(response).sendError(
            eq(HttpServletResponse.SC_NOT_FOUND),
            anyString()
        );
    }

    @Test
    void validUserHasPrimaryGym() throws IOException {
        // given a user with a primary gym
        assertFalse(userHasPrimaryGym(userId));
        assertTrue(setPrimaryGym(userId, gymId));

        // embed the user id in the session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(userId);

        // mock the request, response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // when calling the servlet
        new StoreGymServlet().doPost(request, response);

        // verify the results
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // after: remove primary gym relationship
        assertTrue(removeUserPrimaryGym(userId));
    }
}
