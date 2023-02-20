package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.json.JSONArray;
import org.json.JSONObject;
import pbrg.webservices.models.Gym;
import pbrg.webservices.models.Route;
import pbrg.webservices.models.RouteFull;
import pbrg.webservices.models.User;

public final class DatabaseController {

    private DatabaseController() {
    }

    /**
     * Get DB connection.
     *
     * @return DB connection
     */
    public static Connection getDbConnection() throws SQLException {
        // TODO - pass implementation properties to InitialContext
        Connection connection = null;
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:/comp/env");
            DataSource ds = (DataSource) envContext.lookup("jdbc/grabourg");

            // create and return new connection
            connection = ds.getConnection();
        } catch (NamingException exception) {
            System.out.println(exception.getMessage());
        }

        assert (connection != null);
        return connection;
    }

    /**
     * Sign a user in.
     *
     * @param username username
     * @param password password
     * @return user object
     * @throws SQLException if the SQL query fails
     */
    public static User signIn(
            final String username, final String password) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?"
            )
        ) {
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("uid"), rs.getString("username"));
            }
        }

        return null;
    }

    /**
     * Sign a user up.
     *
     * @param username username
     * @param email    email
     * @param password password
     * @return true if user was added, false otherwise
     * @throws SQLException if SQL error occurs
     */
    public static boolean signUp(
        final String username, final String email, final String password
    ) throws SQLException {
        insertUser(username, email, password);
        return userExists(username);
    }

    private static void insertUser(
        final String username, final String email, final String password
    ) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO users (Username, Email, Password) VALUES (?,?,?)"
            )
        ) {
            String[] values = {username, email, password};
            for (int i = 1; i <= values.length; i++) {
                pst.setString(i, values[i - 1]);
            }
            pst.executeUpdate();
        }
    }

    private static boolean userExists(
            final String username) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username=?)"
            )
        ) {
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        }

        return false;
    }

    /**
     * Get all gyms matching a query word in its location or name.
     *
     * @param queryWord query word
     * @return list of gyms
     * @throws SQLException if database error
     */
    public static List<String> getGymsByQueryWord(
            final String queryWord) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT Gymname "
                    + "FROM gyms "
                    + "WHERE GymLocation LIKE ? OR Gymname LIKE ?"
            )
        ) {
            pst.setString(1, "%" + queryWord + "%");
            pst.setString(2, "%" + queryWord + "%");
            ResultSet rs = pst.executeQuery();

            List<String> gyms = new ArrayList<>();
            while (rs.next()) {
                gyms.add(rs.getString("Gymname"));
            }
            return gyms;
        }
    }

    /**
     * Get a gym by name.
     *
     * @param gymName The name of the gym.
     * @return The gym.
     * @throws SQLException If SQL query fails.
     */
    public static Gym getGymByGymName(
            final String gymName) throws SQLException {
        try (
                Connection connection = getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT GID, Gymname FROM gyms WHERE Gymname = ?")) {
            pst.setString(1, gymName);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int gid = rs.getInt("GID");
                String rGymName = rs.getString("Gymname");
                return new Gym(gid, rGymName);
            }
        }

        return null;
    }

    /**
     * Get a gym by a user ID.
     *
     * @param userId user ID
     * @return a gym
     * @throws SQLException if SQL error occurs
     */
    public static Gym getGymByUserId(final int userId) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID, Gymname "
                    + "FROM gyms "
                    + "WHERE GID = (SELECT GID FROM user_in_gym WHERE UID = ?)"
            )
        ) {
            pst.setInt(1, userId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int gid = rs.getInt("GID");
                String gymName = rs.getString("Gymname");
                return new Gym(gid, gymName);
            }
        }

        return null;
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
            final int gymId, final int userId) throws SQLException {
        try (
            Connection connection = getDbConnection();
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
            List<Route> routes = new ArrayList<>();
            while (rs.next()) {
                routes.add(new Route(
                        rs.getInt("RID"),
                        rs.getInt("Difficulty"),
                        "Route #" + rs.getInt("RID")));
            }

            return routes;
        }
    }

    /**
     * Get a wall image file name by wall id.
     * @param wallId the wall id
     * @return the wall image file name
     * @throws SQLException If the query fails
     */
    public static String getWallImageFileNameFromWallId(final int wallId)
            throws SQLException {
        try (
                Connection connection = getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT walls.image_file_name "
                                + "FROM walls "
                                + "WHERE WID = ?")) {
            pst.setInt(1, wallId);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            if (rs.next()) {
                return rs.getString("image_file_name");
            }
        }

        return null;
    }

    private static RouteFull getRouteByRouteId(final int routeId)
            throws SQLException {
        try (
                Connection connection = getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT * "
                                + "FROM routes "
                                + "WHERE routes.RID = ?")) {
            pst.setInt(1, routeId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new RouteFull(
                        rs.getInt("RID"),
                        rs.getInt("WID"),
                        rs.getInt("creator_user_id"),
                        rs.getInt("Difficulty"),
                        rs.getString("RouteContent"),
                        rs.getString("image_file_name"));
            }
        }

        return null;
    }

    /**
     * Returns a route image file name for a given route id.
     *
     * @param routeId the route id of the route
     * @return the route image file name
     * @throws SQLException if there is an error with the database
     */
    public static String getRouteImageFileNamesByRouteId(
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
        try (
            Connection connection = getDbConnection();
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
                return rs.getBoolean(1);
            }
        }

        return false;
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
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT routes.route_content "
                    + "FROM routes "
                    + "WHERE RID = ?")) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                return rs.getString("route_content");
            }
        }

        return null;
    }

    /**
     * Get a route's contents (list of holds) as a JSON array.
     * @param routeId route identifier
     * @return list of holds in JSON
     * @throws SQLException database issues
     */
    public static JSONArray getRouteContentJSONArray(
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
    public static JSONObject getRouteContentJSONObject(
        final int routeId
    ) throws SQLException {
        return new JSONObject(Objects.requireNonNull(getRouteContent(routeId)));
    }

    /**
     * Get a wall ID from a route ID.
     * @param routeId route identifier
     * @return wall identifier
     * @throws SQLException database issues
     */
    public static Integer getWallIdFromRouteId(
        final int routeId
    ) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT routes.WID "
                    + "FROM routes "
                    + "WHERE RID = ?")) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                return Integer.parseInt(rs.getString("WID"));
            }
        }

        return null;
    }

    /**
     * Returns the wall image file name based on the route ID.
     * @param routeId the route ID
     * @return the wall image file name if found, null otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static String getWallImageFileNameFromRouteId(
        final int routeId
    ) throws SQLException {
        Integer wallId = getWallIdFromRouteId(routeId);
        if (wallId == null) {
            return null;
        }
        return getWallImageFileNameFromWallId(wallId);
    }

    /**
     * Get a gym ID from a gym ID.
     * @param gymId gym identifier
     * @return wall identifier
     */
    public static Integer getWallIdFromGymId(
        final int gymId
    ) throws SQLException {
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT walls.WID "
                    + "FROM walls "
                    + "WHERE GID = ?")) {
            pst.setInt(1, gymId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                return Integer.parseInt(rs.getString("WID"));
            }
        }

        return null;
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
        try (
            Connection connection = getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO routes "
                    + "(route_content, difficulty, creator_user_id, WID) "
                    + "VALUES (?, ?, ?, ?) "
                + "RETURNING RID"
            )
        ) {
            Object[] values = {routeContent, difficulty, creatorUserId, wallId};
            String[] types = {"String", "int", "int", "int"};

            for (int i = 1; i <= values.length; i++) {
                Object value = values[i];
                String type = types[i];

                if (type == "String") {
                    pst.setString(i, (String) value);
                } else if (type == "int") {
                    pst.setInt(i, (int) value);
                }
            }
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                return Integer.parseInt(rs.getString("RID"));
            }
        }

        return null;
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
            Connection connection = getDbConnection();
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
