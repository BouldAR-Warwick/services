package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.RouteController.userOwnsRoute;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

@WebServlet(name = "StoreRouteServlet", urlPatterns = "/GetRoute")
public class StoreRouteServlet extends MyHttpServlet {

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
        boolean requiresSession = true;
        String userIdKey = "uid";
        String[] sessionAttributes = {userIdKey};
        String routeIdKey = "routeID";
        JSONObject body = getBodyAsJson(request);
        String[] bodyAttributes = {routeIdKey};
        if (!validateRequest(
            request, response, body, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // get user ID from session attribute
        HttpSession session = getSession(request);
        assert session != null;
        int userID = (int) session.getAttribute(userIdKey);

        // get route ID from request body
        assert body != null;
        int routeID = (int) body.get(routeIdKey);

        // ensure the user id is valid
        if (!userExists(userID)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "User ID does not exist"
            );
            return;
        }

        // ensure the route exists
        if (!routeExists(routeID)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Route ID does not exist"
            );
            return;
        }

        // ensure this user created this route
        boolean userCreatedRoute = userOwnsRoute(userID, routeID);
        if (!userCreatedRoute) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "User did not create this route"
            );
            return;
        }

        // store route ID in session attribute
        session.setAttribute("rid", routeID);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
