package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import pbrg.webservices.models.User;
import pbrg.webservices.utils.DatabaseController;

@WebServlet(name = "RegisterServlet", urlPatterns = "/Register")
public class RegisterServlet extends MyHttpServlet {

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

        // parse credentials
        JSONObject credentials;
        try {
            credentials = new JSONObject(getBody(request));
            // Iterator<String> it = credentials.keys();
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // ensure request has all credentials
        String[] requiredCredentials = {"username", "email", "password"};
        if (!Arrays.stream(requiredCredentials).allMatch(credentials::has)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // store credentials
        String username = credentials.getString("username");
        String email = credentials.getString("email");
        String password = credentials.getString("password");

        // create new user
        boolean added = false;
        try {
            added = DatabaseController.signUp(username, email, password);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
        }

        // ensure user was added
        if (!added) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // select user
        User user = null;
        try {
            user = DatabaseController.signIn(username, password);
        } catch (Exception e) {
            response.getWriter().println(e.getMessage());
        }

        // case one -> the user has not been authenticated (wrong credentials)
        boolean userLoggedIn = (user != null);
        if (!userLoggedIn) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // case two -> user is authenticated

        HttpSession session = request.getSession();
        session.setAttribute("uid", user.getUid());

        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
