package pbrg.webservices.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * For static utils: \ getting database connection, file path utils, API utils.
 */
public final class Utils {

    /**
     * Default radius for route image generation.
     */
    private static final int DEFAULT_RADIUS = 10;

    /**
     * Default color for route image generation.
     */
    private static final Scalar DEFAULT_COLOUR = new Scalar(0, 0, 255);

    /**
     * Path to wall image directory.
     */
    public static final String WALL_IMAGE_PATH =
        System.getProperty("user.home") + "/wall-images/";

    /**
     * Path to route image directory.
     */
    public static final String ROUTE_IMAGE_PATH =
        System.getProperty("user.home") + "/route-images/";

    /**
     * Map from file extensions to content types.
     */
    private static final Map<String, String> CONTENT_TYPE_MAP =
        Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("pbg", "image/png")
          /*
          unsupported:
              image/gif
              image/tiff
              image/vnd.microsoft.icon
              image/x-icon
              image/vnd.djvu
              image/svg+xml
          */
        );

    /**
     * Seven days.
     */
    public static final int SEVEN_DAYS = 7;

    private Utils() {
    }

    /**
     * Get a content type from a file extension.
     *
     * @param imageFormat file extension
     * @return content type
     */
    public static String getContentType(final String imageFormat) {
        if (imageFormat == null) return null;
        return CONTENT_TYPE_MAP.get(imageFormat);
    }

    /**
     * Check if a session has required attributes.
     * @param session session
     * @param requiredSessionAttributes string array of required attributes
     * @return true if session has all required attributes
     */
    public static boolean sessionHasAttributes(
        final HttpSession session,
        final String[] requiredSessionAttributes
    ) {
        return Arrays
            .stream(requiredSessionAttributes)
            .allMatch(
                attribute -> session.getAttribute(attribute) != null
            );
    }

    /**
     * Generate a route on a 2016 Moonboard.
     * @param grade grade
     * @return route as a JSON object of holds
     */
    public static JSONObject generateRouteMoonboard(final int grade) {
        ProcessBuilder pb = new ProcessBuilder(
            "python", "route-gen-moonboard.py", String.valueOf(grade)
        );
        Process process = null;

        try {
            process = pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream())
        );
        StringBuilder output = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int exitCode = 0;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (exitCode != 0) {
            throw new RuntimeException(
                "Route generation failed with exit code " + exitCode
            );
        }

        // result is a comma separated list of coordinates \
        // (hold positions to be used)
        String result = output.toString();

        // parse result as a json object
        JSONObject route = new JSONObject(result);

        return route;
    }

    /**
     * Create a 2d route image by highlighting holds on a wall.
     * @param routeId route identifier
     * @return file name of the route image
     * @throws SQLException database issues
     */
    public static String createRouteImage(
        final int routeId
    ) throws SQLException {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Load the image file
        String wallImageFileName = DatabaseController
            .getWallImageFileNameFromRouteId(routeId);
        Mat image = Imgcodecs.imread(wallImageFileName);

        // Parse the JSON string into a JSON array
        JSONArray holdArray = DatabaseController
            .getRouteContentJSONArray(routeId);

        // Loop through each hold in the JSON array
        for (int i = 0; i < holdArray.length(); i++) {
            // Get the current hold
            JSONObject hold = holdArray.getJSONObject(i);

            // Get the x and y coordinates of the hold
            double x = hold.getDouble("x");
            double y = hold.getDouble("y");

            // Scale the coordinates to the size of the image
            int xScaled = (int) (x * image.width());
            int yScaled = (int) (y * image.height());

            // Draw a circle at the location of the hold
            Imgproc.circle(
                image,
                new Point(xScaled, yScaled),
                DEFAULT_RADIUS,
                DEFAULT_COLOUR,
                -1
            );
        }

        // Save the modified image
        String routeImageFileName = "r" + routeId + "-" + wallImageFileName;
        Imgcodecs.imwrite(routeImageFileName, image);

        return routeImageFileName;
    }
}
