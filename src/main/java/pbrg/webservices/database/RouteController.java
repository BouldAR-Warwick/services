package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public static @NotNull List<Route> getRoutesInGymMadeByUser(
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

    /**
     * Get a route by its ID.
     * @param routeId route ID
     * @return route
     */
    public static RouteFull getRouteByRouteId(
        final int routeId
    ) {
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
                    rs.getString("route_content"),
                    rs.getString("image_file_name")
                );
            }
        } catch (SQLException e) {
            return null;
        }
        return route;
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
     * Check if a route, by ID, exists.
     * @param routeId route ID
     * @return route exists
     */
    public static boolean routeExists(
        final int routeId
    ) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS("
                    + "SELECT 1 FROM routes "
                    + "WHERE routes.RID=?"
                    + ")"
            )
        ) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
        return exists;
    }

    /**
     * Get a route's contents (list of holds) as a String.
     * @param routeId route identifier
     * @return list of holds in JSON
     */
    public static @Nullable String getRouteContent(
        final int routeId
    ) {
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
        } catch (SQLException e) {
            return null;
        }
        return holds;
    }

    /**
     * Create a new route (without image) in the database.
     * @param routeContent route content (list of holds)
     * @param difficulty route difficulty
     * @param creatorUserId creator user identifier
     * @param wallId wall identifier
     * @return route identifier
     */
    public static Integer addRoute(
        final String routeContent, final int difficulty,
        final int creatorUserId, final int wallId
    ) {
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

            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                String type = types[i];

                if (type.equals("String")) {
                    pst.setString(i + 1, (String) value);
                } else {
                    pst.setInt(i + 1, (int) value);
                }
            }
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();

            if (rs.next()) {
                // the route ID
                routeId = rs.getInt(1);
            }
        } catch (SQLException e) {
            return null;
        }
        return routeId;
    }

    /**
     * Add a route image to an existing route.
     * @param routeId route identifier
     * @param imageFileName route image file name
     * @return true if image was added, false otherwise
     * @throws SQLException database issues
     */
    public static boolean addImageToRoute(
        final int routeId, final String imageFileName
    ) throws SQLException {
        boolean updated;
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

            // the number of rows affected by the update query
            int rowsAffected = pst.executeUpdate();
            updated = rowsAffected > 0;
        }
        return updated;
    }

    /**
     * Delete a route from the database.
     * @param creatorUserId creator user identifier
     * @param wallId wall identifier
     * @return true if route was deleted, false otherwise
     */
    public static boolean deleteRoute(
        final int creatorUserId, final int wallId
    ) {
        boolean removed;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM routes "
                    + "WHERE creator_user_id = ? AND WID = ?"
            )
        ) {
            pst.setInt(1, creatorUserId);
            pst.setInt(2, wallId);
            removed = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
        return removed;
    }

    /**
     * Delete a route from the database.
     * @param routeId route identifier
     * @return true if route was deleted, false otherwise
     */
    public static boolean deleteRoute(
        final int routeId
    ) {
        boolean removed;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM routes "
                    + "WHERE rid = ?"
            )
        ) {
            pst.setInt(1, routeId);
            removed = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
        return removed;
    }
}
