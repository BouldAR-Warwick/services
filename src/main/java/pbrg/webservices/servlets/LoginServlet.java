package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import pbrg.webservices.models.User;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.Utils;

@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {

        // convert request body to json object
        JSONObject credentials;
        try {
            credentials = new JSONObject(getBody(request));
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // ensure request has all credentials
        String[] requiredCredentials = {"username", "password", "stayLoggedIn"};
        if (!Arrays.stream(requiredCredentials).allMatch(credentials::has)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // collect credentials
        String username = credentials.getString("username");
        String password = credentials.getString("password");
        boolean stayLoggedIn = credentials.getBoolean("stayLoggedIn");

        // select user
        User user;
        try {
            user = DatabaseController.signIn(username, password);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // case one -> the user has not been authenticated (wrong credentials)
        boolean userLoggedIn = (user != null);
        if (!userLoggedIn) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // user is authenticated
        HttpSession session = request.getSession();
        session.setAttribute("uid", user.getUid());

        // TODO store primary gym id in session?

        if (stayLoggedIn) {
            // create cookie and store logged-in user info in cookie
            Cookie cookie1 = new Cookie("username", user.getUsername());
            Cookie cookie2 = new Cookie("uid", String.valueOf(user.getUid()));

            // set expired time to 7 days
            int sevenDaysInSeconds =
                (int) Duration.ofDays(Utils.SEVEN_DAYS).getSeconds();
            cookie1.setMaxAge(sevenDaysInSeconds);
            cookie2.setMaxAge(sevenDaysInSeconds);

            // send cookie back to client for authentication next time
            response.addCookie(cookie1);
            response.addCookie(cookie2);
        }

        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
