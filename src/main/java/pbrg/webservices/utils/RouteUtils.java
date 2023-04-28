package pbrg.webservices.utils;

import static pbrg.webservices.database.ProductionDatabase.production;
import static pbrg.webservices.database.RouteController.addImageToRoute;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.WallController.getWallImageFileNameFromRouteId;
import static pbrg.webservices.utils.ProcessUtils.runProcessEnsureSuccess;
import static pbrg.webservices.utils.ProcessUtils.runProcessGetOutputEnsureSuccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import pbrg.webservices.database.RouteController;
import pbrg.webservices.models.RouteFull;

public final class RouteUtils {

    static {
        // set the python scripts directory based on environment
        resetPythonScriptsDirectory();
    }

    /** The path to the python scripts directory. */
    private static String pythonScriptsDir;

    /** The file name for the route generation script. */
    private static String routeGenerationScript = "route-gen-moon-board.py";

    /** The file name for the hold plotting script. */
    private static String holdPlottingScript = "plot-holds.py";

    /** Util class, no instances. */
    private RouteUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Reset the python scripts directory based on environment.
     */
    public static void resetPythonScriptsDirectory() {
        setPythonScriptsDir(production());
    }

    /**
     * Set the hold plotting script.
     * @param script the hold plotting script
     */
    public static void setHoldPlottingScript(final String script) {
        holdPlottingScript = script;
    }

    /**
     * Get the hold plotting script.
     * @return the hold plotting script
     */
    public static String getHoldPlottingScript() {
        return holdPlottingScript;
    }

    /**
     * Configure the route generation script.
     * @param script the route generation script
     */
    public static void setRouteGenerationScript(final String script) {
        routeGenerationScript = script;
    }

    /**
     * Get the route generation script.
     * @return the route generation script
     */
    public static String getRouteGenerationScript() {
        return routeGenerationScript;
    }

    /**
     * Set the python scripts directory based on the environment.
     * @param inProduction true if in production, false otherwise
     */
    public static void setPythonScriptsDir(final boolean inProduction) {
        // if in production
        if (inProduction) {
            pythonScriptsDir = System.getProperty("user.home")
                + "/Projects/services/scripts/python/";
            return;
        }

        // otherwise in test
        pythonScriptsDir = System.getProperty("user.dir") + "/scripts/python/";
    }

    /**
     * Set the path to the python scripts directory.
     * @param dir path to the python scripts directory
     */
    static void setPythonScriptsDir(final String dir) {
        pythonScriptsDir = dir;
    }

    /**
     * Get the path to the python scripts directory.
     * @return path to the python scripts directory
     */
    static String getPythonScriptsDir() {
        return pythonScriptsDir;
    }

    /**
     * Generate a route on a 2016 MoonBoard.
     * @param grade grade
     * @return route as a JSON object of holds
     */
    @Contract("_ -> new")
    public static @NotNull JSONArray generateRouteMoonBoard(final int grade)
        throws IOException {
        // path is working dir + python-scripts/plot-holds.py
        File pythonFile = new File(
            getPythonScriptsDir(), getRouteGenerationScript()
        );

        // ensure file exists
        if (!pythonFile.exists()) {
            throw new IOException(
                "Python script " + pythonFile + " does not exist"
            );
        }

        ProcessBuilder pb = new ProcessBuilder(
            "python3",
            pythonFile.toString(),
            String.valueOf(grade)
        );

        // run py script, collect results
        // comma separated list of coordinates (hold positions to be used)
        StringBuilder output = runProcessGetOutputEnsureSuccess(pb);
        String result = output.toString();

        // return route - parse result as a json object
        return new JSONArray(result);
    }

    /**
     * Create a 2D route image by highlighting holds on a wall \
     * with a Python script.
     * @param routeId route ID
     * @return file name of the route image
     */
    public static @Nullable String createRouteImagePython(
        final int routeId
    ) {
        // ensure the route exists
        if (!routeExists(routeId)) {
            return null;
        }

        // note every route has a wall

        // load the wall image file
        String wallImageFileName = getWallImageFileNameFromRouteId(routeId);
        assert wallImageFileName != null;

        // Parse the JSON string into a JSON array
        JSONArray holdArray = getRouteContentJSONArray(routeId);
        if (holdArray == null) {
            // route has no holds
            return null;
        }

        // plot holds on image by calling python script
        String routeFileName;
        try {
            routeFileName = plotHoldsOnImagePython(
                routeId, wallImageFileName,
                ServletUtils.getWallImagePath(),
                ServletUtils.getRouteImagePath(),
                holdArray
            );
        } catch (IOException e) {
            return null;
        }
        return routeFileName;
    }

    /**
     * Create and store a route image for a route by id.
     * @param routeId route id
     * @return true if successful, false otherwise
     */
    public static boolean createAndStoreRouteImage(final int routeId) {
        // ensure the route exists
        if (!routeExists(routeId)) {
            return false;
        }

        // note every route has a wall

        // create the route image
        String routeImageFileName = createRouteImagePython(routeId);
        if (routeImageFileName == null) {
            return false;
        }

        // ensure routeImageFileName exists
        File routeImage =
            new File(ServletUtils.getRouteImagePath(), routeImageFileName);
        assert routeImage.exists();

        // store the route image
        return addImageToRoute(routeId, routeImageFileName);
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
    public static @NotNull String plotHoldsOnImagePython(
        final int routeId,
        @NotNull final String wallImageFileName,
        @NotNull final String wallImageFilePath,
        @NotNull final String routeImageFilePath,
        @NotNull final JSONArray holdArray
    ) throws IOException {
        // path is working dir + python-scripts/plot-holds.py
        File pythonFile = new File(
            getPythonScriptsDir(), getHoldPlottingScript()
        );

        // ensure file exists
        if (!pythonFile.exists()) {
            throw new IOException(
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
        runProcessEnsureSuccess(pb);

        // return the file name of the route image
        return "r" + routeId + "-" + wallImageFileName;
    }

    /**
     * Get a route's contents (list of holds) as a JSON array.
     * @param routeId route identifier
     * @return list of holds in JSON
     */
    public static @Nullable JSONArray getRouteContentJSONArray(
        final int routeId
    ) {
        // ensure the route exists
        if (!routeExists(routeId)) {
            return null;
        }
        String routeContent = RouteController.getRouteContent(routeId);
        assert routeContent != null;
        try {
            return new JSONArray(routeContent);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Returns a route image file name for a given route id.
     *
     * @param routeId the route id of the route
     * @return the route image file name
     */
    public static @Nullable String getRouteImageFileName(
        final int routeId
    ) {
        // ensure the route exists
        if (!routeExists(routeId)) {
            return null;
        }
        RouteFull route = RouteController.getRouteByRouteId(routeId);
        assert route != null;
        return route.getImageFileName();
    }

    /**
     * Delete a route by its id (including its image file).
     * @param routeId route id
     */
    public static void deleteRoute(final int routeId) {
        // ensure route exists
        if (!routeExists(routeId)) {
            return;
        }

        String routeImageFileName = getRouteImageFileName(routeId);
        boolean routeImageGenerated = routeImageFileName != null;

        // remove route image if it exists, do nothing if it doesn't exist
        if (routeImageGenerated) {
            deleteRouteImage(routeImageFileName);
            assert !new File(ServletUtils.getRouteImagePath(),
                routeImageFileName).exists();
        }

        // remove from database
        RouteController.deleteRoute(routeId);
    }

    /**
     * Delete a route image file. Does nothing if the file does not exist.
     * @param routeImageFileName route image file name
     */
    private static void deleteRouteImage(final String routeImageFileName) {
        // create the file system path
        File routeImageFile = new File(
            ServletUtils.getRouteImagePath(), routeImageFileName
        );

        try {
            // delete the file
            Files.delete(routeImageFile.toPath());
        } catch (IOException e) {
            // IOException -> file does not exist
        }
    }
}
