package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class RegisterServletTest {

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
}