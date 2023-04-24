package pbrg.webservices.utils;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static pbrg.webservices.database.ProductionDatabase.production;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jetbrains.annotations.NotNull;

/**
 * For servlet utils: file path utils, API utils (bitmap returns).
 */
public final class ServletUtils {

    static {
        setPaths(production());
    }

    /** The path to the wall images directory. */
    private static String wallImagePath;

    /** The path to the route images directory. */
    private static String routeImagePath;

    /**
     * Get the path to the wall images directory.
     * @return the path to the wall images directory
     */
    public static String getWallImagePath() {
        return wallImagePath;
    }

    /**
     * Set the image paths.
     * @param inProduction true if in production, false otherwise
     */
    public static void setPaths(final boolean inProduction) {
        // if in production
        if (inProduction) {
            wallImagePath = System.getProperty("user.home")
                + "/Projects/services/src/main/resources/";
            routeImagePath = System.getProperty("user.home")
                + "/Projects/services/src/main/resources/";
            return;
        }

        // otherwise in test
        wallImagePath = System.getProperty("user.dir")
            + "/src/main/resources/";
        routeImagePath = System.getProperty("user.dir")
            + "/src/main/resources/";
    }

    /**
     * Get the path to the route images directory.
     * @return the path to the route images directory
     */
    public static String getRouteImagePath() {
        return routeImagePath;
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
            Map.entry("png", "image/png")
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
     * Return an image as a byte array.
     * @param filePath file name
     * @return byte array
     * @throws IOException file errors
     */
    static byte[] getBytesFromFile(final String filePath) throws IOException {
        byte[] imageBuffer;
        try (FileInputStream fis = new FileInputStream(filePath)) {
            imageBuffer = getBytesFromFileInputStream(fis);
        }
        return imageBuffer;
    }

    /**
     * Return an image as a byte array from a file input stream.
     * @param fis file input stream
     * @return byte array
     * @throws IOException file errors
     */
    static byte[] getBytesFromFileInputStream(
        final FileInputStream fis
    ) throws IOException {
        int size = fis.available();
        byte[] imageBuffer = new byte[size];
        int bytesRead = fis.read(imageBuffer);
        if (size != bytesRead) {
            throw new IOException(
                "Expected " + size + " bytes, read " + bytesRead
            );
        }
        return imageBuffer;
    }

    /**
     * Return an image as a bitmap.
     * @param response response
     * @param filePath file name
     * @throws IOException file errors
     */
    static void returnImageAsBitmap(
        @NotNull final HttpServletResponse response,
        @NotNull final String filePath
    ) throws IOException {
        // get the file extension, lookup & set content type
        String ext = getExtension(filePath);
        String contentType = getContentType(ext);
        if (contentType == null) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            throw new IOException("Unsupported file type: " + ext);
        }
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer = getBytesFromFile(filePath);

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
        @NotNull final HttpServletResponse response,
        @NotNull final String fileName
    ) throws IOException {
        returnImageAsBitmap(
            response,
            ServletUtils.wallImagePath + fileName
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
            ServletUtils.routeImagePath + fileName
        );
    }
}
