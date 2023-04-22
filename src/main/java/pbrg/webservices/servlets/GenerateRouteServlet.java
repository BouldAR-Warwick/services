package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.SQLException;
import pbrg.webservices.utils.Utils;
import static pbrg.webservices.database.RouteController.addImageToRoute;
import static pbrg.webservices.database.RouteController.createRoute;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.getWallIdFromGymId;
import static pbrg.webservices.database.WallController.gymHasWall;
import static pbrg.webservices.utils.Utils.createRouteImagePython;
import static pbrg.webservices.utils.Utils.generateRouteMoonBoard;
import static pbrg.webservices.utils.Utils.returnImageAsBitmap;

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
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    /**
     * given wall ID and grade, generate a route.
     * */
    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        // get session
        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // parse arguments
        JSONObject arguments;
        try {
            arguments = new JSONObject(getBody(request));
        } catch (JSONException e) {
            System.out.println("Issue parsing body as JSON");
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Issue parsing body as JSON."
            );
            return;
        }

        // ensure request has all credentials
        String difficultyKey = "difficulty";
        if (!arguments.has(difficultyKey)) {
            System.out.println("Missing difficulty");
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Body missing difficulty."
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

        if (!gymHasWall(gymId)) {
            System.out.println("Gym does not have a wall");
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Gym does not have a wall"
            );
            return;
        }

        // generate the route
        JSONArray route;
        try {
            route = generateRouteMoonBoard(grade);
        } catch (RuntimeException e) {
            System.out.println("Route generation failed");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route generation failed"
            );
            return;
        }

        // get the wall ID
        int wallID;
        try {
            wallID = getWallIdFromGymId(gymId);
        } catch (SQLException | NullPointerException e) {
            System.out.println("Failed to get wall ID from gym ID.");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to get wall ID from gym ID."
            );
            return;
        }

        // store the route as a new route in the database
        int routeId;
        try {
            routeId = createRoute(
                route.toString(), grade, userId, wallID
            );
        } catch (SQLException | NullPointerException e) {
            System.out.println("Failed to create route.");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route."
            );
            return;
        }

        System.out.println("Route created: " + route);
        System.out.println("Route ID: " + routeId);

        /*
        // set wall and route images path to resources
        Utils.wallImagePath = "src/main/resources/";
        Utils.routeImagePath = "src/main/resources/";

        // generate route image, store filename
        String newFile;
        try {
            newFile = createRouteImagePython(gymId);

            // reset directories
            Utils.wallImagePath = Utils.DEFAULT_WALL_IMAGE_PATH;
            Utils.routeImagePath = Utils.DEFAULT_ROUTE_IMAGE_PATH;
        } catch (Exception e) {
            System.out.println("Failed to create route image.");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route image."
            );
            return;
        }

        // store route image in database
        try {
            addImageToRoute(routeId, newFile);
        } catch (SQLException e) {
            System.out.println("Failed to add image to route.");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to add image to route."
            );
            return;
        }
         */

        System.out.println("Route generated successfully.");
        //returnImageAsBitmap(response, newFile);

        // return the route
        JSONObject routeJSON = new JSONObject();
        try {
            routeJSON.put("route", route);
        } catch (JSONException e) {
            System.out.println("Failed to create JSON object.");
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create JSON object."
            );
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(routeJSON.toString());
    }
}
