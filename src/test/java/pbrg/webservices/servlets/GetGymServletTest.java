package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.getGymByGymName;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.database.GymControllerTest;

class GetGymServletTest {

    /** Mock the request object. */
    @Mock
    private HttpServletRequest request;

    /** Mock the response object. */
    @Mock
    private HttpServletResponse response;

    /** Mock the session object. */
    @Mock
    private HttpSession session;

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
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        GetRoutesServlet servlet = spy(GetRoutesServlet.class);

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void testInvalidRequest() throws IOException {
        // given invalid request
        when(request.getSession(false)).thenReturn(session);
        when(request.getInputStream())
            .thenReturn(mock(ServletInputStream.class));

        // assert: verify that doGet does not throw an exception
        assertDoesNotThrow(
            // act: getting gym servlet
            () -> new GetGymServlet().doPost(request, response)
        );
    }

    @Test
    void failingWithNullSession() throws IOException {
        // arrange
        when(request.getSession(false)).thenReturn(null);

        // assert: verify that doGet does not throw an exception
        assertDoesNotThrow(
            // act: getting gym servlet
            () -> new GetGymServlet().doPost(request, response)
        );

        // assert: verify that unauthorized error is sent
        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void passingButNoGymsMatch() throws IOException {
        String gymNameKey = "gymname";
        String gymName = "";
        assertNull(getGymByGymName(gymName));

        String requestBody = String.format("{%s:'%s'}", gymNameKey, gymName);
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        request = mockRequestWithBody(requestBody);
        when(request.getSession(anyBoolean()))
            .thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling register
        new GetGymServlet().doPost(request, response);

        // ensure the writer is called
        verify(response).getWriter();

        // then ensure the response is SC_NOT_FOUND
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void passingWithMatchingGym() throws IOException {
        // construct the test user
        int gymId = createTestGym();

        String gymNameKey = "gymname";
        String gymName = GymControllerTest.TEST_GYM_NAME;
        assertNotNull(getGymByGymName(gymName));

        String requestBody = String.format("{%s:'%s'}", gymNameKey, gymName);
        assertDoesNotThrow(
            () -> new JSONObject(requestBody)
        );
        request = mockRequestWithBody(requestBody);
        when(request.getSession(anyBoolean()))
            .thenReturn(mock(HttpSession.class));
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        // when calling register
        new GetGymServlet().doPost(request, response);

        // ensure the writer is called
        verify(response).getWriter();

        // then ensure the response is SC_OK
        verify(response).setStatus(HttpServletResponse.SC_OK);

        // after: delete the test user
        assertTrue(deleteGym(gymId));
        assertFalse(gymExists(gymId));
    }
}
