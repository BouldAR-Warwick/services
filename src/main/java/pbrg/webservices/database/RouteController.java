package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import pbrg.webservices.models.Route;
import pbrg.webservices.models.RouteFull;

public final class RouteController {

    /** Static class, no need to instantiate. */
    private RouteController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a list of routes in a gym, created by a user.
     *
     * @param gymId  gym ID
     * @param userId creator user ID
     * @return list of routes
     * @throws SQLException if SQL error occurs
     */
    public static List<Route> getRoutesInGymMadeByUser(
        final int gymId, final int userId
    ) throws SQLException {
        List<Route> routes = new ArrayList<>();
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT * "
                    + "FROM routes "
                    + "INNER JOIN walls ON routes.WID = walls.WID "
                    + "INNER JOIN gyms ON walls.GID = gyms.GID "
                    + "WHERE gyms.GID = ? AND routes.creator_user_id = ?"
            )
        ) {
            pst.setInt(1, gymId);
            pst.setInt(2, userId);

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                routes.add(new Route(
                    rs.getInt("RID"),
                    rs.getInt("Difficulty"),
                    "Route #" + rs.getInt("RID")
                ));
            }
        }
        return routes;
    }

    private static RouteFull getRouteByRouteId(
        final int routeId
    ) throws SQLException {
        RouteFull route = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT * "
                    + "FROM routes "
                    + "WHERE routes.RID = ?"
            )
        ) {
            pst.setInt(1, routeId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                route = new RouteFull(
                    rs.getInt("RID"),
                    rs.getInt("WID"),
                    rs.getInt("creator_user_id"),
                    rs.getInt("Difficulty"),
                    rs.getString("RouteContent"),
                    rs.getString("image_file_name")
                );
            }
        }

        return route;
    }

    /**
     * Returns a route image file name for a given route id.
     *
     * @param routeId the route id of the route
     * @return the route image file name
     * @throws SQLException if there is an error with the database
     */
    public static @Nullable String getRouteImageFileNamesByRouteId(
        final int routeId) throws SQLException {
        RouteFull route = getRouteByRouteId(routeId);

        if (route == null) {
            return null;
        }

        return route.getImageFileName();
    }

    /**
     * Check if a user, by ID, has created a route, by ID.
     *
     * @param userId  creator user ID
     * @param routeId route ID
     * @return user has created route
     * @throws SQLException Query or database get fails
     */
    public static boolean userOwnsRoute(
        final int userId, final int routeId
    ) throws SQLException {
        boolean ownsRoute = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS("
                    + "SELECT 1 FROM routes "
                    + "WHERE routes.RID=? AND routes.creator_user_id = ?"
                    + ")"
            )
        ) {
            pst.setInt(1, routeId);
            pst.setInt(2, userId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                ownsRoute = rs.getBoolean(1);
            }
        }

        return ownsRoute;
    }

    /**
     * Get a route's contents (list of holds) as a String.
     * @param routeId route identifier
     * @return list of holds in JSON
     * @throws SQLException database issues
     */
    public static String getRouteContent(
        final int routeId
    ) throws SQLException {
        String holds = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT routes.route_content "
                    + "FROM routes "
                    + "WHERE RID = ?"
            )
        ) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                holds = rs.getString("route_content");
            }
        }

        return holds;
    }

    /**
     * Get a route's contents (list of holds) as a JSON array.
     * @param routeId route identifier
     * @return list of holds in JSON
     * @throws SQLException database issues
     */
    @Contract("_ -> new")
    public static @NotNull JSONArray getRouteContentJSONArray(
        final int routeId
    ) throws SQLException {
        return new JSONArray(Objects.requireNonNull(getRouteContent(routeId)));
    }

    /**
     * Get a route's contents (list of holds) as a JSON object.
     * @param routeId route identifier
     * @return list of holds in JSON
     * @throws SQLException database issues
     */
    @Contract("_ -> new")
    public static @NotNull JSONObject getRouteContentJSONObject(
        final int routeId
    ) throws SQLException {
        return new JSONObject(Objects.requireNonNull(getRouteContent(routeId)));
    }

    /**
     * Create a new route (without image) in the database.
     * @param routeContent route content (list of holds)
     * @param difficulty route difficulty
     * @param creatorUserId creator user identifier
     * @param wallId wall identifier
     * @return route identifier
     * @throws SQLException database issues
     */
    public static Integer createRoute(
        final String routeContent, final int difficulty,
        final int creatorUserId, final int wallId
    ) throws SQLException {
        Integer routeId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO routes "
                    + "(route_content, difficulty, creator_user_id, WID) "
                    + "VALUES (?, ?, ?, ?) ",
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            Object[] values =
                {routeContent, difficulty, creatorUserId, wallId};
            String[] types = {"String", "int", "int", "int"};

            for (int i = 1; i <= values.length; i++) {
                Object value = values[i];
                String type = types[i];

                if (type.equals("String")) {
                    pst.setString(i, (String) value);
                } else if (type.equals("int")) {
                    pst.setInt(i, (int) value);
                }
            }
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next()) {
                // the route ID
                routeId = rs.getInt(1);
            }
        }

        // Route creation: no keys returned.
        return routeId;
    }

    /**
     * Add a route image to an existing route.
     * @param routeId route identifier
     * @param imageFileName route image file name
     * @throws SQLException database issues
     */
    public static void addImageToRoute(
        final int routeId, final String imageFileName
    ) throws SQLException {
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "UPDATE routes "
                    + "SET image_file_name = ? "
                    + "WHERE RID = ?"
            )
        ) {
            pst.setString(1, imageFileName);
            pst.setInt(2, routeId);
            pst.executeUpdate();
        }
    }
}
