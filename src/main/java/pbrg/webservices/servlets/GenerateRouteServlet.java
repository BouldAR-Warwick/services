package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.getWallIdFromGymId;
import static pbrg.webservices.database.WallController.gymHasWall;
import static pbrg.webservices.utils.RouteUtils.createAndStoreRouteImage;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import pbrg.webservices.database.RouteController;
import pbrg.webservices.database.WallController;

@WebServlet(
    name = "GenerateRouteServlet",
    urlPatterns = "/GenerateRoute"
)
public class GenerateRouteServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    /**
     * given wall ID and grade, generate a route.
     * */
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
        String difficultyKey = "difficulty";
        JSONObject body = getBodyAsJson(request);
        String[] bodyAttributes = {difficultyKey};
        if (!validateRequest(
            request, response, body, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // get arguments
        HttpSession session = getSession(request);
        assert session != null;
        int userId = (int) session.getAttribute("uid");
        int gymId = (int) session.getAttribute("gid");

        // get the difficulty
        assert body != null;
        int grade = body.getInt(difficultyKey);

        // ensure the user exists
        if (!userExists(userId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "User does not exist"
            );
            return;
        }

        // ensure the gym exists
        if (!gymExists(gymId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Gym does not exist"
            );
            return;
        }

        // ensure the gym has a wall; if not, mock one
        boolean mockingWall = false;
        if (!gymHasWall(gymId)) {
            // add a wall using the MoonBoard wall
            addWall(gymId, "MoonBoard", "MoonBoard2016.jpg");
            mockingWall = true;
        }
        assert gymHasWall(gymId);

        // get the wall ID
        Integer wallID = getWallIdFromGymId(gymId);
        assert wallID != null;

        // generate the route
        JSONArray route;
        try {
            route = generateRouteMoonBoard(grade);
            assert !route.isEmpty();
        } catch (IOException | RuntimeException e) {
            // cleanup the wall, if mocked
            if (mockingWall) {
                WallController.deleteWall(wallID);
            }
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Route generation failed" + e.getMessage()
            );
            return;
        }

        // store the route as a new route in the database
        Integer routeId = addRoute(
            route.toString(), grade, userId, wallID
        );
        assert routeId != null;

        // generate the route image (thumbnail)
        boolean routeImageGenerated = createAndStoreRouteImage(routeId);
        if (!routeImageGenerated) {
            // cleanup the route, wall (if mocked)
            RouteController.deleteRoute(routeId);
            if (mockingWall) {
                WallController.deleteWall(wallID);
            }
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route image."
            );
            return;
        }

        // Route generated successfully

        // return the route id and route
        JSONObject responseJSON = new JSONObject();
        responseJSON.put("routeId", routeId);
        response.setContentType("application/json");
        response.getWriter().write(responseJSON.toString());

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
