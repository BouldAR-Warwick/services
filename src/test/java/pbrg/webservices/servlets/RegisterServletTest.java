package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.AuthenticationController.deleteUser;
import static pbrg.webservices.database.AuthenticationController.getUserIDFromUsername;
import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.DatabaseTestMethods.mockThrowsExceptionOnGetConnection;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import javax.sql.DataSource;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.AuthenticationControllerTest;
import pbrg.webservices.database.DatabaseController;

class RegisterServletTest {

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

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        String requestBody = "";
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        RegisterServlet servlet = spy(new RegisterServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void failingSignUpFailed() throws IOException {
        // given a failed sign-up
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockThrowsExceptionOnGetConnection());

        String usernameKey = "username";
        String emailKey = "email";
        String passwordKey = "password";

        String username = AuthenticationControllerTest.TEST_USERNAME;
        String email = AuthenticationControllerTest.TEST_EMAIL;
        String password = AuthenticationControllerTest.TEST_PASSWORD;

        String requestBody = String.format(
            "{%s:%s,%s:%s,%s:%s}",
            usernameKey, username,
            emailKey, email,
            passwordKey, password
        );
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling register
        new RegisterServlet().doPost(request, response);

        // then ensure the response is SC_EXPECTATION_FAILED
        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after: reset the data source
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void passing() throws IOException {
        String usernameKey = "username";
        String emailKey = "email";
        String passwordKey = "password";

        String username = AuthenticationControllerTest.TEST_USERNAME;
        String email = AuthenticationControllerTest.TEST_EMAIL;
        String password = AuthenticationControllerTest.TEST_PASSWORD;

        String requestBody = String.format(
            "{%s:%s,%s:%s,%s:%s}",
            usernameKey, username,
            emailKey, email,
            passwordKey, password
        );
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        HttpServletRequest request = mockRequestWithBody(requestBody);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling register
        new RegisterServlet().doPost(request, response);

        // then ensure user was created
        Integer userId = getUserIDFromUsername(username);
        assertNotNull(userId);
        assertTrue(userExists(userId));

        // then ensure the response is SC_OK
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // after: reset the user
        assertTrue(deleteUser(userId));
        assertFalse(userExists(userId));
    }
}
