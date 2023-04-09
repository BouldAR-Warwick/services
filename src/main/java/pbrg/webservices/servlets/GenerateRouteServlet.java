package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import pbrg.webservices.utils.DatabaseController;
import pbrg.webservices.utils.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // ensure request has all credentials
        String[] requiredArguments = {"difficulty"};
        if (!Arrays.stream(requiredArguments).allMatch(arguments::has)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int userId = (int) session.getAttribute("uid");
        int gymId = (int) session.getAttribute("gid");
        int grade = arguments.getInt("difficulty");

        // generate the route
        JSONArray route = Utils.generateRouteMoonboard(grade);
        if (route == null) {
            System.out.println("Route generation failed");
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the wall ID
        Integer wallID;
        try {
            wallID = DatabaseController.getWallIdFromGymId(gymId);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (wallID == null) {
            System.out.println("No wall found for gym ID: " + gymId);
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }


        // store the route as a new route in the database
        Integer routeId;
        try {
            routeId = DatabaseController.createRoute(
                route.toString(), grade, userId, wallID
            );
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        assert routeId != null;

        // generate route image, store filename
        String newFile;
        try {
            newFile = Utils.createRouteImagePython(gymId);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // wall query failed or no wall against gym
        if (newFile == null) {
            // case gym has no wall! - TODO
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // store route image in database
        try {
            DatabaseController.addImageToRoute(routeId, newFile);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // return the new image as a bitmap

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(newFile);
        String contentType = Utils.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer;
        try (
            FileInputStream fis = new FileInputStream(
                Utils.WALL_IMAGE_PATH + newFile
            )
        ) {
            int size = fis.available();
            imageBuffer = new byte[size];
            int bytesRead = fis.read(imageBuffer);

            if (size != bytesRead) {
                response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
                return;
            }
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(imageBuffer);
            outputStream.flush();
        }
    }
}
