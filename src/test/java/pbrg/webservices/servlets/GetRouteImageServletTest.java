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

class GetRouteImageServletTest {

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        GetRouteImageServlet servlet = spy(new GetRouteImageServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Disabled
    @Test
    void failingInvalidRouteId() {
        // given an invalid route id
        fail();
    }

    @Disabled
    @Test
    void failingRouteWithoutImage() {
        // given a route without an image
        fail();
    }

    @Disabled
    @Test
    void failingRouteImageNotFound() {
        // given a route with an image that does not exist
        fail();
    }

    @Disabled
    @Test
    void failingRouteImageNotReturned() {
        // given a route with an image that cannot be returned
        fail();
    }

    @Disabled
    @Test
    void passing() {
        // given a route with an image that can be returned
        fail();
    }
}
