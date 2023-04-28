package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GetRoutesServletTest {

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        GetRoutesServlet servlet = spy(new GetRoutesServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Disabled
    @Test
    void failingInvalidUser() {
        // given an invalid user
        fail();
    }

    @Disabled
    @Test
    void failingInvalidGym() {
        // given an invalid gym
        fail();
    }

    @Disabled
    @Test
    void passing() {
        // given a valid user and gym
        fail();
    }
}
