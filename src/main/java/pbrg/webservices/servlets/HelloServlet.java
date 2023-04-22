package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {

    /** test message. */
    private String message;

    /**
     * initialise message.
     */
    @Override
    public void init() {
        message = "Hello World!";
    }

    /**
     * This method is called by the server (via the service method) \
     * to allow a servlet to handle a GET request.
     */
    @Override
    public final void doGet(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws IOException {
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }
}
