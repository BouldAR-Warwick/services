package pbrg.webservices.utils;

import java.util.Arrays;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

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
}
