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
        // validate request
        boolean requiresSession = false;
        String[] sessionAttributes = {};
        String usernameKey = "username";
        String emailKey = "email";
        String passwordKey = "password";
        JSONObject body = getBodyAsJson(request);
        String[] bodyAttributes = {usernameKey, emailKey, passwordKey};
        if (!validateRequest(
            request, response, body, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // store credentials
        assert body != null;
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
        assert user != null;

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
