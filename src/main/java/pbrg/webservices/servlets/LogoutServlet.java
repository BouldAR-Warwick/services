package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.URLDecoder;

@WebServlet(name = "LogoutServlet", urlPatterns = "/Logout")
public class LogoutServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // set cookie to 0 second valid (a.k.a. deleted)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(URLDecoder.decode(cookie.getName(), StandardCharsets.UTF_8));
                if (URLDecoder.decode(cookie.getName(), StandardCharsets.UTF_8).equals("username")) {
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
