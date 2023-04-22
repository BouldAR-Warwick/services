package pbrg.webservices.utils;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;

/**
 * For servlet utils: file path utils, API utils (bitmap returns).
 */
public final class ServletUtils {

    /** The path to the wall images directory. */
    private static final String WALL_IMAGE_PATH = "src/main/resources/";

    /** The path to the route images directory. */
    private static final String ROUTE_IMAGE_PATH = "src/main/resources/";

    /**
     * Get the path to the wall images directory.
     * @return the path to the wall images directory
     */
    public static String getWallImagePath() {
        return WALL_IMAGE_PATH;
    }

    /**
     * Get the path to the route images directory.
     * @return the path to the route images directory
     */
    public static String getRouteImagePath() {
        return ROUTE_IMAGE_PATH;
    }

    /**
     * Map from file extensions to content types.
     * unsupported:
     * image/gif, image/tiff, image/vnd.microsoft.icon
     * image/x-icon, image/vnd.djvu, image/svg+xml
     */
    private static final Map<String, String> CONTENT_TYPE_MAP =
        Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("pbg", "image/png")
        );

    /**
     * Seven days.
     */
    public static final int SEVEN_DAYS = 7;

    /** Static class, no need to instantiate. */
    private ServletUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a content type from a file extension.
     *
     * @param imageFormat file extension
     * @return content type
     */
    public static String getContentType(final String imageFormat) {
        if (imageFormat == null) {
            return null;
        }
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
     * Return an image as a bitmap.
     * @param response response
     * @param filePath file name
     * @throws IOException file errors
     */
    private static void returnImageAsBitmap(
        final HttpServletResponse response, final String filePath
    ) throws IOException {
        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(filePath);
        String contentType = ServletUtils.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer;
        try (
            FileInputStream fis = new FileInputStream(
                filePath
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

    /**
     * Return a wall image as a bitmap.
     * @param response response
     * @param fileName file name
     * @throws IOException file errors
     */
    public static void returnWallImageAsBitmap(
        final HttpServletResponse response, final String fileName
    ) throws IOException {
        returnImageAsBitmap(
            response,
            ServletUtils.WALL_IMAGE_PATH + fileName
        );
    }

    /**
     * Return a route image as a bitmap.
     * @param response response
     * @param fileName file name
     * @throws IOException file errors
     */
    public static void returnRouteImageAsBitmap(
        final HttpServletResponse response, final String fileName
    ) throws IOException {
        returnImageAsBitmap(
            response,
            ServletUtils.ROUTE_IMAGE_PATH + fileName
        );
    }
}
