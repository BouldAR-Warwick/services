package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.RouteController
    .getRoutesInGymMadeByUser;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pbrg.webservices.models.Route;

@WebServlet(name = "GetRoutesServlet", urlPatterns = "/GetRoutes")
public class GetRoutesServlet extends MyHttpServlet {

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
        String gymIdKey = "gid";
        String[] sessionAttributes = {userIdKey, gymIdKey};
        String[] bodyAttributes = {};
        if (!validateRequest(
            request, response, null, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // collect user id, gym id from cookies
        HttpSession session = getSession(request);
        assert session != null;
        int userId = (int) session.getAttribute("uid");
        int gymId = (int) session.getAttribute("gid");

        // ensure the user id is valid
        if (!userExists(userId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "User ID does not exist"
            );
            return;
        }

        // ensure the gym id is valid
        if (!gymExists(gymId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Gym ID does not exist"
            );
            return;
        }

        // get routes in gym made by user
        List<Route> routes = getRoutesInGymMadeByUser(gymId, userId);
        Route[] arrayOfRoutes = routes.toArray(new Route[0]);

        // place list of routes in response body
        JSONObject responseBody = new JSONObject();
        responseBody.put("routes", arrayOfRoutes);

        // send response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseBody.toString());

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
