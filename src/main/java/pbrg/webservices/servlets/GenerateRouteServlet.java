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
import static pbrg.webservices.database.WallController.getWallIdFromGymId;
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
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Issue parsing body as JSON."
            );
            return;
        }

        // ensure request has all credentials
        String difficultyKey = "difficulty";
        if (!arguments.has(difficultyKey)) {
            response.getWriter().println("Missing difficulty");
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Body missing difficulty."
            );
            return;
        }

        int userId = (int) session.getAttribute("uid");
        int gymId = (int) session.getAttribute("gid");
        int grade = arguments.getInt(difficultyKey);

        // generate the route
        JSONArray route;
        try {
            route = Utils.generateRouteMoonBoard(grade);
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
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to create route."
            );
            return;
        }

        // generate route image, store filename
        String newFile;
        try {
            newFile = Utils.createRouteImagePython(gymId);
        } catch (Exception e) {
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
            e.printStackTrace();
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Failed to add image to route."
            );
            return;
        }

        returnImageAsBitmap(response, newFile);
    }
}
