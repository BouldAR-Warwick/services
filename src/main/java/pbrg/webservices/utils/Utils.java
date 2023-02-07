package pbrg.webservices.utils;

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
     * Create a 2d route image by highlighting holds on a wall.
     * @param routeId route identifier
     * @throws SQLException database issues
     */
    public static void createRouteImage(final int routeId) throws SQLException {
        // Load the OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Load the image file
        String wallImageFileName = DatabaseController.getWallImageFileNameFromRouteId(routeId);
        Mat image = Imgcodecs.imread(wallImageFileName);

        // Parse the JSON string into a JSON array
        JSONArray holdArray = DatabaseController.getRouteContentJSONArray(routeId);

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
                image, new Point(xScaled, yScaled), 10, new Scalar(0, 0, 255), -1
            );
        }

        // Save the modified image
        Imgcodecs.imwrite("r" + routeId + "-" + wallImageFileName, image);
    }
}
