package pbrg.webservices.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

final class HelloServletTest {

    @Test
    void testDoGet() throws Exception {
        HelloServlet servlet = new HelloServlet();
        servlet.init();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        // when: doGet is called
        servlet.doGet(request, response);

        // then: Hello World! is a substring of result
        String result = stringWriter.toString();
        System.out.println(result);
        assertTrue(result.contains("Hello World!"));
    }
}
