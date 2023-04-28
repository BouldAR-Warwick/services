package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

@WebServlet(name = "LogoutServlet", urlPatterns = "/Logout")
public class LogoutServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) {
        // ensure request has cookies
        Cookie[] cookies = request.getCookies();

        // invalidate cookies, if they exist
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String field = URLDecoder.decode(
                    cookie.getName(), StandardCharsets.UTF_8
                );
                if (field.equals("username")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
                if (field.equals("uid")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        // invalidate session
        request.getSession(false).invalidate();

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
