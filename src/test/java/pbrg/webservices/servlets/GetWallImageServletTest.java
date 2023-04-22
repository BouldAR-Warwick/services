package pbrg.webservices.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetWallImageServletTest {

    /** The servlet to test. */
    private static final GetWallImageServlet SERVLET =
        new GetWallImageServlet();

    @Test
    void doGetWithNulls() {
        assertThrows(
            // then: throws NullPointerException
            NullPointerException.class,

            // when: requested with null request and response
            () -> SERVLET.doGet(null, null)
        );
    }

    @Test
    void emptyResponse() {
        // given: valid request, null response
        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(
            // then: throws NullPointerException
            NullPointerException.class,

            // when: requested with null request and response
            () -> SERVLET.doGet(request, null)
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
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
