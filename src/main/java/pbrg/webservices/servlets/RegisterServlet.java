package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.signIn;
import static pbrg.webservices.database.AuthenticationController.signUp;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
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
        JSONObject body = getBodyAsJson(request);
        if (body == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not a valid JSON object"
            );
            return;
        }

        // ensure request has all credentials
        String usernameKey = "username";
        String emailKey = "email";
        String passwordKey = "password";
        String[] requiredCredentials = {usernameKey, emailKey, passwordKey};
        for (String credential : requiredCredentials) {
            if (!body.has(credential)) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Request body is missing " + credential
                );
                return;
            }
        }

        // store credentials
        String username = body.getString(usernameKey);
        String email = body.getString(emailKey);
        String password = body.getString(passwordKey);

        // create new user; ensure user was added
        boolean added = signUp(username, email, password);
        if (!added) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Failed to sign-up user"
            );
            return;
        }

        // select user
        User user = signIn(username, password);

        // case one: the user has not been authenticated (wrong credentials)
        if (user == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid credentials"
            );
            return;
        }

        // case two: user is authenticated

        // store user id in session
        HttpSession session = request.getSession();
        session.setAttribute("uid", user.getUid());

        // return the user object as json
        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
