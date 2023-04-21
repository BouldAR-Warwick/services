package pbrg.webservices.utils;

import static pbrg.webservices.database.RouteController.getRouteContentJSONArray;
import static pbrg.webservices.database.WallController.getWallImageFileNameFromRouteId;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

/**
 * For static utils: getting database connection, file path utils, API utils.
 */
public final class Utils {

    /** The current working directory. */
    private static final File WORKING_DIR =
        new File(System.getProperty("user.dir"));

    /** The path to the python scripts directory. */
    private static final String PYTHON_SCRIPTS_DIR =
        WORKING_DIR + "/scripts/python/";

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
        throw new IllegalStateException("Utility class");
    }

    /**
     * Run a process.
     * @param pb process builder
     * @return process
     */
    private static Process runProcess(final ProcessBuilder pb) {
        // read the output printed by the python script
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return process;
    }

    /**
     * Collect output from a process.
     * @param process process
     * @return output as a string builder, each line is a new line
     */
    private static @NotNull StringBuilder collectOutput(
        @NotNull final Process process
    ) {
        StringBuilder output = new StringBuilder();
        for (String line : collectOutputAsList(process)) {
            output.append(line);
            output.append(System.lineSeparator());
        }

        return output;
    }

    /**
     * Collect output from a process in a list of strings.
     * @param process process
     * @return output as a list of strings, each string is a line
     */
    public static @NotNull List<String> collectOutputAsList(
        final @NotNull Process process
    ) {
        List<String> output = new ArrayList<>();

        String line;
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            )
        ) {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return output;
    }

    /**
     * Get exit code from a process.
     * @param process process
     * @return exit code
     */
    private static int getExitCode(final Process process) {
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return exitCode;
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
     * Generate a route on a 2016 MoonBoard.
     * @param grade grade
     * @return route as a JSON object of holds
     */
    public static JSONArray generateRouteMoonBoard(final int grade) {
        // path is working dir + python-scripts/plot-holds.py
        File pythonFile = new File(
            PYTHON_SCRIPTS_DIR,
            "route-gen-moon-board.py"
        );

        // ensure file exists
        if (!pythonFile.exists()) {
            throw new RuntimeException(
                "Python script " + pythonFile + " does not exist"
            );
        }

        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            pythonFile.toString(),
            String.valueOf(grade)
        );

        // run py script, collect results
        Process process = runProcess(pb);
        StringBuilder output = collectOutput(process);

        // ensure success
        int exitCode = getExitCode(process);
        if (exitCode != 0) {
            throw new RuntimeException(
                "Route generation failed with exit code " + exitCode
            );
        }

        // comma separated list of coordinates (hold positions to be used)
        String result = output.toString();

        // return route - parse result as a json object
        return new JSONArray(result);
    }

    /**
     * Create a 2D route image by highlighting holds on a wall \
     * with a Python script.
     * @param routeId route ID
     * @return file name of the route image
     * @throws SQLException database errors
     */
    public static String createRouteImagePython(
        final int routeId
    ) throws SQLException {
        // Load the image file
        String wallImageFileName = getWallImageFileNameFromRouteId(routeId);

        // Parse the JSON string into a JSON array
        JSONArray holdArray = getRouteContentJSONArray(routeId);

        // plot holds on image by calling python script
        return plotHoldsOnImagePython(
            routeId, wallImageFileName,
            Utils.WALL_IMAGE_PATH, Utils.ROUTE_IMAGE_PATH,
            holdArray
        );
    }

    /**
     * Plot holds on an image using python script plot-holds.py.
     * @param routeId route id
     * @param wallImageFileName wall image file name
     * @param wallImageFilePath wall image file path
     * @param routeImageFilePath route image file path
     * @param holdArray json array of holds
     * @return new file name
     */
    public static String plotHoldsOnImagePython(
        final int routeId,
        final String wallImageFileName,
        final String wallImageFilePath,
        final String routeImageFilePath,
        final JSONArray holdArray
    ) {
        // path is working dir + python-scripts/plot-holds.py
        File pythonFile = new File(
            PYTHON_SCRIPTS_DIR,
            "plot-holds.py"
        );

        // ensure file exists
        if (!pythonFile.exists()) {
            throw new RuntimeException(
                "Python script " + pythonFile + " does not exist"
            );
        }

        // python script with arguments: wallImageFileName, routeID, holdArray
        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            pythonFile.toString(),
            wallImageFileName,
            wallImageFilePath,
            routeImageFilePath,
            String.valueOf(routeId),
            holdArray.toString()
        );

        // run hold plotting script
        Process process = runProcess(pb);

        // ensure success
        int exitCode = getExitCode(process);
        if (exitCode != 0) {
            throw new RuntimeException(
                "Route thumbnail generation failed with exit code " + exitCode
            );
        }

        // return the file name of the route image
        return "r" + routeId + "-" + wallImageFileName;
    }

    /**
     * Return an image as a bitmap.
     * @param response response
     * @param fileName file name
     * @throws IOException file errors
     */
    public static void returnImageAsBitmap(
        final HttpServletResponse response, final String fileName
    ) throws IOException {
        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(fileName);
        String contentType = Utils.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer;
        try (
            FileInputStream fis = new FileInputStream(
                Utils.WALL_IMAGE_PATH + fileName
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
