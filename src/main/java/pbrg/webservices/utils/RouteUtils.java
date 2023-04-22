package pbrg.webservices.utils;

import static pbrg.webservices.database.RouteController.getRouteContentJSONArray;
import static pbrg.webservices.database.WallController.getWallImageFileNameFromRouteId;
import static pbrg.webservices.utils.ProcessUtils.collectOutput;
import static pbrg.webservices.utils.ProcessUtils.getExitCode;
import static pbrg.webservices.utils.ProcessUtils.runProcess;

import java.io.File;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

public final class RouteUtils {

    /** The current working directory. */
    private static final File WORKING_DIR =
        new File(System.getProperty("user.dir"));

    /** The path to the python scripts directory. */
    private static final String PYTHON_SCRIPTS_DIR =
        WORKING_DIR + "/scripts/python/";

    /** Util class, no instances. */
    private RouteUtils() {
        throw new IllegalStateException("Utility class");
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
        if (holdArray.isEmpty()) {
            throw new RuntimeException(
                "Route " + routeId + " has no holds"
            );
        }

        if (wallImageFileName == null) {
            throw new RuntimeException(
                "Route " + routeId + " has no wall image"
            );
        }

        // plot holds on image by calling python script
        return plotHoldsOnImagePython(
            routeId, wallImageFileName,
            ServletUtils.getWallImagePath(), ServletUtils.getRouteImagePath(),
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
        @NotNull final String wallImageFileName,
        @NotNull final String wallImageFilePath,
        @NotNull final String routeImageFilePath,
        @NotNull final JSONArray holdArray
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
}
