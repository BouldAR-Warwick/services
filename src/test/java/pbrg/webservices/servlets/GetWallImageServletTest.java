package pbrg.webservices.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetWallImageServletTest {

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

    @Disabled
    @Test
    void failingInvalidGym() {
        // given a gym that does not exist
        fail();
    }

    @Disabled
    @Test
    void failingGymWithoutWall() {
        // given a gym that does not have a wall
        fail();
    }

    @Disabled
    @Test
    void failingWallImageFileNotFound() {
        // given a gym that has a wall but no local image
        fail();
    }

    @Disabled
    @Test
    void failingWallImageNotReturned() {
        // given a gym that has a wall and a local image, but return fails
        fail();
    }

    @Disabled
    @Test
    void passing() {
        // given a gym that has a wall and a local image
        fail();
    }
}
