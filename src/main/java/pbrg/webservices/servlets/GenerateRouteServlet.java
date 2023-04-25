package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.SQLException;
import pbrg.webservices.utils.ServletUtils;

import static pbrg.webservices.database.RouteController.addImageToRoute;
import static pbrg.webservices.database.RouteController.addRoute;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.getWallIdFromGymId;
import static pbrg.webservices.database.WallController.gymHasWall;
import static pbrg.webservices.utils.RouteUtils.createRouteImagePython;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;

/**
 * prototyping only for the MoonBoard wall.
 * run route generation to return list of holds and \
        // coordinates to be used in route:
        // - direct call to python script
        // - or call to tensorflow REST API / java tensorflow
        //   - however these will require additional processing \
        // which would have to be written on java side
        //   - => will just stick with python call
        // require a restructuring of service classes \
    // so that Holds exist as children of wall not associated with routes
 */
@WebServlet(
    name = "GenerateRouteServlet",
    urlPatterns = "/GenerateRouteServlet"
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
        HttpSession session = getSession(request);

        // ensure session is valid
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ensure session has attributes
        boolean sessionHasAttributes = session.getAttribute("uid") != null
            && session.getAttribute("gid") != null;
        if (!sessionHasAttributes) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not have attributes user, gym ids."
            );
            return;
        }

        // parse arguments
        JSONObject arguments;
        try {
            arguments = new JSONObject(getBody(request));
        } catch (JSONException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Issue parsing body as JSON."
            );
            return;
        }

        // ensure request has all credentials
        String difficultyKey = "difficulty";
        if (!arguments.has(difficultyKey)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Body missing route grade."
            );
            return;
        }

        int userId = (int) session.getAttribute("uid");
        int gymId = (int) session.getAttribute("gid");
        int grade = arguments.getInt(difficultyKey);

        // mock the wall
        Integer wallId;
        if (!gymHasWall(gymId)) {
            // add a wall using the MoonBoard wall
            wallId = addWall(
                gymId, "MoonBoard", "MoonBoard2016.jpg"
            );
            assert wallId != null;
        }

        // ensure the gym has a wall (mocked above)
        if (!gymHasWall(gymId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Gym does not have a wall"
            );
            return;
        }

        // get the wall ID
        Integer wallID = getWallIdFromGymId(gymId);
        assert wallID != null;

        // generate the route
        JSONArray route;
        try {
            route = generateRouteMoonBoard(grade);
            if (route.isEmpty()) {
                throw new IllegalStateException("Route generation failed");
            }
        } catch (IllegalStateException | IOException e) {
            // Route generation failed
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route generation failed"
            );
            return;
        }

        // store the route as a new route in the database
        Integer routeId = addRoute(
            route.toString(), grade, userId, wallID
        );
        if (routeId == null) {
            // Failed to create route
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route."
            );
            return;
        }

        // generate route image, store filename
        String routeImageFileName = createRouteImagePython(routeId);
        if (routeImageFileName == null) {
            // Failed to create route image
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route image, IOException thrown."
            );
            return;
        }

        // ensure routeImageFileName exists
        File routeImage =
            new File(ServletUtils.getRouteImagePath(), routeImageFileName);
        if (!routeImage.exists()) {
            // Failed to create route image
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route image, file not found."
            );
            return;
        }

        // store route image in database
        try {
            addImageToRoute(routeId, routeImageFileName);
        } catch (SQLException e) {
            // Failed to add image to route
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to add image to route."
            );
            return;
        }

        // Route generated successfully

        // return the route id and route
        JSONObject responseJSON = new JSONObject();
        responseJSON.put("routeId", routeId);
        response.setContentType("application/json");
        response.getWriter().write(responseJSON.toString());
    }
}
