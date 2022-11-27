package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pbrg.webservices.models.Gym;
import pbrg.webservices.models.Route;
import pbrg.webservices.models.User;

public final class Database {

    private Database() {
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
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT * FROM users WHERE username=? AND password=?")) {
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
            final String username, final String email, final String password) throws SQLException {
        insertUser(username, email, password);
        return userExists(username);
    }

    private static void insertUser(
            final String username, final String email, final String password) throws SQLException {
        try (
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "INSERT INTO users (Username, Email, Password) VALUES (?,?,?)")) {
            String[] values = { username, email, password };
            for (int i = 1; i <= values.length; i++) {
                pst.setString(i, values[i]);
            }
            pst.executeUpdate();
        }
    }

    private static boolean userExists(
            final String username) throws SQLException {
        try (
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT EXISTS(SELECT 1 FROM users WHERE username=?)")) {
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
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT Gymname "
                                + "FROM gyms "
                                + "WHERE GymLocation LIKE ? OR Gymname LIKE ?")) {
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
            Connection connection = Utils.getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID, Gymname FROM gyms WHERE Gymname = ?"
            )
        ) {
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
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT GID, Gymname "
                                + "FROM gyms "
                                + "WHERE GID = (SELECT GID FROM user_in_gym WHERE UID = ?)")) {
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
     * Get a list of routes in a gym, by gym ID.
     *
     * @param gymId gym ID
     * @return list of routes
     * @throws SQLException if SQL error occurs
     */
    public static List<String> getRouteIdsByGymId(final int gymId)
            throws SQLException {
        try (
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT routes.RID "
                                + "FROM routes "
                                + "INNER JOIN walls ON routes.WID = walls.WID "
                                + "INNER JOIN gyms ON walls.GID = gyms.GID "
                                + "WHERE gyms.GID = ?")) {
            pst.setInt(1, gymId);

            ResultSet rs = pst.executeQuery();
            List<String> routeIds = new ArrayList<>();
            while (rs.next()) {
                routeIds.add(rs.getString("RID"));
            }
            return routeIds;
        }
    }

    /**
     * Get a wall image file name by gym id.
     *
     * @param gymId the gym id
     * @return the wall image file name
     * @throws SQLException If the query fails
     */
    public static String getWallImageFileNameFromGymId(final int gymId)
            throws SQLException {
        try (
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT walls.image_file_name "
                                + "FROM walls "
                                + "WHERE GID = ?")) {
            pst.setInt(1, gymId);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            if (rs.next()) {
                return rs.getString("image_file_name");
            }
        }

        return null;
    }

    private static Route getRouteByRouteId(final int routeId)
            throws SQLException {
        try (
                Connection connection = Utils.getDbConnection();
                PreparedStatement pst = connection.prepareStatement(
                        "SELECT * "
                                + "FROM routes "
                                + "WHERE routes.RID = ?")) {
            pst.setInt(1, routeId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new Route(
                        rs.getInt("RID"),
                        rs.getInt("WID"),
                        rs.getInt("creator_user_id"),
                        rs.getInt("Difficulty"),
                        rs.getString("RouteContent"),
                        rs.getString("RouteContent"));
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
        Route route = getRouteByRouteId(routeId);

        if (route == null) {
            return null;
        }

        return route.getImageFileName();
    }
}
