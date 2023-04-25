package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.signIn;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pbrg.webservices.models.User;
import pbrg.webservices.utils.ServletUtils;

@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet extends MyHttpServlet {

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
        // parse request body to json object
        JSONObject arguments = getBodyAsJson(request);
        if (arguments == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not a valid JSON object"
            );
            return;
        }

        // ensure request has all credentials
        String usernameKey = "username";
        String passwordKey = "password";
        String persistKey = "stayLoggedIn";
        String[] credentials = {usernameKey, passwordKey, persistKey};
        for (String credential : credentials) {
            if (!arguments.has(credential)) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Request body is missing " + credential
                );
                return;
            }
        }

        // collect credentials
        String username = arguments.getString(usernameKey);
        String password = arguments.getString(passwordKey);
        boolean stayLoggedIn = arguments.getBoolean(persistKey);

        User user = signIn(username, password);
        if (user == null) {
            // first case -> user not been authenticated (wrong credentials)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // user is authenticated
        HttpSession session = request.getSession();
        session.setAttribute("uid", user.getUid());

        if (stayLoggedIn) {
            // create cookie and store logged-in user info in cookie
            Cookie cookie1 = new Cookie(usernameKey, user.getUsername());
            Cookie cookie2 = new Cookie("uid", String.valueOf(user.getUid()));

            // set expired time to 7 days
            int sevenDaysInSeconds =
                (int) Duration.ofDays(ServletUtils.SEVEN_DAYS).getSeconds();
            cookie1.setMaxAge(sevenDaysInSeconds);
            cookie2.setMaxAge(sevenDaysInSeconds);

            // send cookie back to client for authentication next time
            response.addCookie(cookie1);
            response.addCookie(cookie2);
        }

        // returning user as json
        String userAsJson = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(userAsJson);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
