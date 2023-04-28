package pbrg.webservices.servlets;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

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
    static @Nullable String getBody(
        final @NotNull HttpServletRequest request
    ) {
        // for constructing the body
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader bufferedReader = request.getReader()) {
            if (bufferedReader == null) {
                throw new IOException("Request body is empty");
            }
            int size = request.getContentLength();
            char[] charBuffer = new char[size];
            int bytesRead;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            return null;
        }

        // return the body
        return stringBuilder.toString();
    }

    /**
     * Get the request body as a json object.
     * @param request the http servlet request
     * @return request body as a json object; null if un-parsable
     */
    public static @Nullable JSONObject getBodyAsJson(
        final @NotNull HttpServletRequest request
    ) {
        String body = getBody(request);
        if (body == null) {
            return null;
        }
        try {
            return new JSONObject(body);
        } catch (JSONException e) {
            return null;
        }
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
            // create a new session for a logged-in user
            int id = Integer.parseInt(uid);
            session = request.getSession(true);
            session.setAttribute("uid", id);
            return session;
        }

        // session doesn't exist and no data in cookies
        return null;
    }

    static boolean validateRequest(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response,
        final @Nullable JSONObject body,
        final boolean sessionRequired,
        final @NotNull String[] sessionAttributes,
        final @NotNull String[] bodyParameters
    ) throws IOException {
        // validate the body, if necessary
        if (!validateBody(response, body, bodyParameters)) {
            return false;
        }

        // validate the session, if necessary
        if (sessionRequired) {
            return validateSession(request, response, sessionAttributes);
        }

        return true;
    }

    /**
     * Validate the body of the request,
     * ensuring it has the required parameters.
     * @param response the http servlet response
     * @param bodyParameters the required body parameters
     * @param body the request body
     * @return true if the body is valid
     * @throws IOException if an input or output error is detected when
     */
    static boolean validateBody(
        final @NotNull HttpServletResponse response,
        final @Nullable JSONObject body,
        final @NotNull String @NotNull [] bodyParameters
    ) throws IOException {
        // ensure we need to check
        if (bodyParameters.length == 0) {
            return true;
        }

        // ensure we have the required body parameters
        if (body == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not valid JSON"
            );
            return false;
        }

        for (String parameter : bodyParameters) {
            boolean inBody = body.has(parameter);
            if (!inBody) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Request body does not have key: " + parameter
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Validate the session, ensuring it exists and has the required attributes.
     * @param request the http servlet request
     * @param response the http servlet response
     * @param sessionAttributes the required session attributes
     * @return true if the session is valid; false otherwise
     * @throws IOException if an input or output error is detected when
     */
    static boolean validateSession(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response,
        final @NotNull String[] sessionAttributes
    ) throws IOException {
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return false;
        }

        // ensure we have the required session attributes
        for (String attribute : sessionAttributes) {
            boolean inSession = session.getAttribute(attribute) != null;
            if (!inSession) {
                response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Session does not have key: " + attribute
                );
                return false;
            }
        }

        return true;
    }
}
