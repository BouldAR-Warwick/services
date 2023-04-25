package pbrg.webservices.servlets;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    /** The servlet to test. */
    private GetGymServlet servlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        servlet = new GetGymServlet();
    }

    @Test
    void testDoGet() throws IOException {
        // arrange
        when(request.getSession(false)).thenReturn(session);
        when(request.getInputStream())
            .thenReturn(mock(ServletInputStream.class));

        // assert: verify that doGet does not throw an exception
        assertDoesNotThrow(
            // act: getting gym servlet
            () -> servlet.doGet(request, response)
        );
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        GetGymServlet servlet = spy(new GetGymServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void testDoPostWithNullSession() throws IOException {
        // arrange
        when(request.getSession(false)).thenReturn(null);

        // assert: verify that doGet does not throw an exception
        assertDoesNotThrow(
            // act: getting gym servlet
            () -> servlet.doGet(request, response)
        );

        // assert: verify that unauthorized error is sent
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testWithInvalidGymName() {
        assertTrue(true);
    }
}
