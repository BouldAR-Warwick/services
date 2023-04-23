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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetGymServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

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

    }
}