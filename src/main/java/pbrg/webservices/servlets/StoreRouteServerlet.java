package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import org.json.JSONException;
import org.json.JSONObject;
import pbrg.webservices.utils.Database;
import pbrg.webservices.utils.Utils;

@WebServlet(name = "StoreRouteServerlet", urlPatterns = "/GetRoute")
public class StoreRouteServerlet extends MyHttpServlet {

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
        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userID = (int) session.getAttribute("uid");

        // convert request body to json object
        JSONObject credentials;
        try {
            credentials = new JSONObject(getBody(request));
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!credentials.has("routeID")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int routeID = (int) credentials.get("routeID");

        // ensure this user created this route

        boolean userCreatedRoute;
        try {
            userCreatedRoute = Database.userOwnsRoute(userID, routeID);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // if the user has not created the route
        if (!userCreatedRoute) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // store route ID in session cookie

        // create cookie and store logged-in user info in cookie
        Cookie routeIdCoolie = new Cookie("rid", String.valueOf(routeID));

        // set expired time to 7 days
        int sevenDaysInSeconds =
            (int) Duration.ofDays(Utils.SEVEN_DAYS).getSeconds();
        routeIdCoolie.setMaxAge(sevenDaysInSeconds);

        // send cookie back to client for authentication next time
        response.addCookie(routeIdCoolie);
    }
}
