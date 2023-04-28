package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.AuthenticationControllerTest.createTestUser;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
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
import pbrg.webservices.database.AuthenticationControllerTest;
import pbrg.webservices.database.DatabaseController;

class LoginServletTest {

    /** The user id of the test user. */
    private int userId;

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
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
        if (userExists(userId)) {
            assertTrue(deleteUser(userId));
            assertFalse(userExists(userId));
        }
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        String requestBody = "";
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        LoginServlet servlet = spy(new LoginServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingSignIn() throws IOException {
        // given a failed sign-in: user does not exist
        String usernameKey = "username";
        String passwordKey = "password";
        String persistKey = "stayLoggedIn";

        String invalidUsername = "nonexistent";
        String password = "password";
        boolean persist = false;

        String requestBody = String.format(
            "{%s:%s,%s:%s,%s:%s}",
            usernameKey, invalidUsername,
            passwordKey, password,
            persistKey, persist
        );
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when calling login
        new LoginServlet().doPost(request, response);

        // then ensure the response is SC_UNAUTHORIZED
        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED), anyString()
        );
    }

    @Test
    void passingAndPersisting() throws IOException {
        // given a successful sign-in, stayLoggedIn is set
        String usernameKey = "username";
        String passwordKey = "password";
        String persistKey = "stayLoggedIn";

        String username = AuthenticationControllerTest.TEST_USERNAME;
        String password = AuthenticationControllerTest.TEST_PASSWORD;
        boolean persist = true;

        String requestBody = String.format(
            "{%s:%s,%s:%s,%s:%s}",
            usernameKey, username,
            passwordKey, password,
            persistKey, persist
        );
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling login
        new LoginServlet().doPost(request, response);

        // then ensure cookies are set, and the response is SC_OK
        verify(response, times(2)).addCookie(any());
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void passingAndNotPersisting() throws IOException {
        // given a successful sign-in, stayLoggedIn is not set
        String usernameKey = "username";
        String passwordKey = "password";
        String persistKey = "stayLoggedIn";

        String username = AuthenticationControllerTest.TEST_USERNAME;
        String password = AuthenticationControllerTest.TEST_PASSWORD;
        boolean persist = false;

        String requestBody = String.format(
            "{%s:%s,%s:%s,%s:%s}",
            usernameKey, username,
            passwordKey, password,
            persistKey, persist
        );
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling login
        new LoginServlet().doPost(request, response);

        // then ensure the response is SC_OK
        verify(response, times(0)).addCookie(any());
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}
