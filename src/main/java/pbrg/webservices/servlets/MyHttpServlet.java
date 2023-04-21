package pbrg.webservices.servlets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MyHttpServlet extends HttpServlet {

    /**
     * Get json object in the request body.
     *
     * @param request the http servlet request
     * @return request body as a json object
     */
    public static String getBody(final HttpServletRequest request)
        throws IOException {

        // for constructing the body
        StringBuilder stringBuilder = new StringBuilder();

        try (
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(request.getInputStream())
            )
        ) {
            int size = request.getInputStream().available();
            char[] charBuffer = new char[size];
            int bytesRead;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        }

        // return the body
        return stringBuilder.toString();
    }

    /**
     * Get session and handle authentication.
     *
     * @param request the http servlet request
     * @return the HttpSession from an HttpServletRequest
     */
    public static HttpSession getSession(final HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            // return existing session
            return session;
        }

        // check cookie for user information
        String uid = "";
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("uid".equals(cookie.getName())) {
                uid = cookie.getValue();
            }
        }
        if (!uid.isEmpty()) {
            // create a new session for stay logged-in user
            int id = Integer.parseInt(uid);
            session = request.getSession(true);
            session.setAttribute("uid", id);
            return session;
        }

        // session doesn't exist and no data in cookies
        return null;
    }
}
