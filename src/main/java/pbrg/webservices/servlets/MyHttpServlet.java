package pbrg.webservices.servlets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyHttpServlet extends HttpServlet {

    /**
     * Override doGet method to support non-null params.
     * @param request an {@link HttpServletRequest} object that contains
     *                the request the client has made of the servlet
     *
     * @param response an {@link HttpServletResponse} object that contains
     *                 the response the servlet sends to the client
     *
     * @throws IOException if an input or output error is detected when
     * @throws IllegalArgumentException if either argument is null
     */
    @Override
    protected void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException, IllegalArgumentException {
        // Override doPost method to support non-null params
    }

    /**
     * Override doPost method to support non-null params.
     * @param request an {@link HttpServletRequest} object that contains
     *                the request the client has made of the servlet
     *
     * @param response an {@link HttpServletResponse} object that contains
     *                 the response the servlet sends to the client
     *
     * @throws IOException if an input or output error is detected when
     * @throws IllegalArgumentException if either argument is null
     */
    @Override
    protected void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException, IllegalArgumentException {
        // Override doPost method to support non-null params
    }

    /**
     * Get json object in the request body.
     *
     * @param request the http servlet request
     * @return request body as a json object
     */
    public static @NotNull String getBody(
        final @NotNull HttpServletRequest request
    ) {

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
        } catch (IOException e) {
            return "";
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
    public static @Nullable HttpSession getSession(
        final @NotNull HttpServletRequest request
    ) {
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
