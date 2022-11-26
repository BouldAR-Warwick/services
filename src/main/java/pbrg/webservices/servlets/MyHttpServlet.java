package pbrg.webservices.servlets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyHttpServlet extends HttpServlet {

    /**
     * Get json object in the request body.
     *
     * @param request the http serverlet request
     * @return request body as a json object
     */
    public static String getBody(final HttpServletRequest request) {

        String body;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(
                    new InputStreamReader(inputStream)
                );
                int size = inputStream.available();
                char[] charBuffer = new char[size];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException ex) {
            // throw ex;
            return "";
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {

                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }

    /**
     * Get session and handle authentication.
     *
     * @param request the http serverlet request
     * @return the HttpSession from an HttpServletRequest
     */
    public static HttpSession getSession(final HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        // return unauthorized error message if session is not exist
        if (session == null) {
            // check cookie for user information
            String uid = "";
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if ("uid".equals(cookie.getName())) {
                    uid = cookie.getValue();
                }
            }
            if (!uid.isEmpty()) {
                // create a new session for stay logged-in user
                int id = Integer.parseInt(uid);
                session = request.getSession();
                session.setAttribute("uid", id);
            }
        }
        return session;
    }
}
