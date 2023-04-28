package pbrg.webservices.servlets;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class LogoutServletTest {

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        when(request.getSession(anyBoolean()))
            .thenReturn(mock(HttpSession.class));
        HttpServletResponse response = mock(HttpServletResponse.class);
        LogoutServlet servlet = spy(new LogoutServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void testDoPostWithCookies() {
        // given a post request with cookies
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        // mock cookies
        Cookie[] cookies = new Cookie[2];
        cookies[0] = new Cookie("username", "testuser");
        cookies[1] = new Cookie("uid", "12345");

        // set up mock behavior
        when(request.getCookies()).thenReturn(cookies);
        when(request.getSession(anyBoolean()))
            .thenReturn(mock(HttpSession.class));

        // when logging out
        new LogoutServlet().doPost(request, response);

        // then ensure cookies are invalidated
        verify(response, times(2))
            .addCookie(argThat(cookie -> cookie.getMaxAge() == 0));
    }
}
