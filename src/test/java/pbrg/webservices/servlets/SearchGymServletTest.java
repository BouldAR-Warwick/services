package pbrg.webservices.servlets;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;

class SearchGymServletTest {

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
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        SearchGymServlet servlet = spy(new SearchGymServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void testDoPostValidCase() throws IOException {
        // create mock objects
        String bodyString = "{\"queryword\":\"test\"}";
        HttpServletRequest request = mockRequestWithBody(bodyString);
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);

        // set up mock behavior
        when(response.getWriter()).thenReturn(writer);
        when(request.getSession(anyBoolean()))
            .thenReturn(mock(HttpSession.class));

        // call the method to test
        new SearchGymServlet().doPost(request, response);

        // verify the results
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(writer).write(anyString());
    }
}
