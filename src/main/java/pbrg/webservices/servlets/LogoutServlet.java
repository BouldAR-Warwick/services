package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "LogoutServlet", urlPatterns = "/Logout")
public class LogoutServlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        // set cookie to 0 second valid (a.k.a. deleted)
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(URLDecoder.decode(cookie.getName(), StandardCharsets.UTF_8));
                if (URLDecoder.decode(cookie.getName(), StandardCharsets.UTF_8)
                    .equals("username")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
                if (URLDecoder.decode(cookie.getName(), StandardCharsets.UTF_8).equals("uid")) {
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }

        // close session
        HttpSession session = request.getSession();
        session.invalidate();
    }
}
