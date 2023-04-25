package pbrg.webservices.servlets;

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
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return;
        }

        // ensure session has user id
        String userIdKey = "uid";
        boolean sessionHasUserId = session.getAttribute(userIdKey) != null;
        if (!sessionHasUserId) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not have the user id"
            );
            return;
        }

        // convert request body to json object
        JSONObject body = getBodyAsJson(request);
        if (body == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not a valid JSON object"
            );
            return;
        }

        // ensure request body contains routeID
        if (!body.has("routeID")) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body does not contain routeID"
            );
            return;
        }

        int userID = (int) session.getAttribute("uid");
        int routeID = (int) body.get("routeID");

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
