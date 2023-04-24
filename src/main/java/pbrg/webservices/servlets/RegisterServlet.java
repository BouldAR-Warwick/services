package pbrg.webservices.servlets;

import static pbrg.webservices.database.CredentialController.signIn;
import static pbrg.webservices.database.CredentialController.signUp;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import pbrg.webservices.models.User;

@WebServlet(name = "RegisterServlet", urlPatterns = "/Register")
public class RegisterServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        // parse credentials
        JSONObject credentials;
        try {
            credentials = new JSONObject(getBody(request));
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // ensure request has all credentials
        String usernameKey = "username";
        String emailKey = "email";
        String passwordKey = "password";
        String[] requiredCredentials = {usernameKey, emailKey, passwordKey};
        if (!Arrays.stream(requiredCredentials).allMatch(credentials::has)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // store credentials
        String username = credentials.getString(usernameKey);
        String email = credentials.getString(emailKey);
        String password = credentials.getString(passwordKey);

        // create new user
        // ensure user was added
        boolean added = signUp(username, email, password);
        if (!added) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // select user
        User user = signIn(username, password);

        // case one -> the user has not been authenticated (wrong credentials)
        if (user == null) {
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
