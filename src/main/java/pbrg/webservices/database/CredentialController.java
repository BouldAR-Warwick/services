package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;
import pbrg.webservices.models.User;

public final class CredentialController {

    /** Static class, no need to instantiate. */
    private CredentialController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sign a user in.
     *
     * @param username username
     * @param password password
     * @return user object
     * @throws SQLException if the SQL query fails
     */
    public static @Nullable User signIn(
        final String username, final String password) throws SQLException {
        User user = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?"
            )
        ) {
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                user = new User(rs.getInt("uid"), rs.getString("username"));
            }
        }

        return user;
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
        boolean added = insertUser(username, email, password);
        if (!added) {
            return false;
        }

        // check for successful creation
        return usernameExists(username);
    }

    /**
     * Insert a user into the database.
     * @param username username
     * @param email email
     * @param password password
     * @return true if user was added, false otherwise
     * @throws SQLException if SQL error occurs
     */
    private static boolean insertUser(
        final String username, final String email, final String password
    ) throws SQLException {
        // ensure username, email are unique
        if (usernameExists(username) || emailExists(email)) {
            return false;
        }

        try (
            Connection connection = getDataSource().getConnection();
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
        return true;
    }

    /**
     * Check if a username exists.
     * @param username username
     * @return true if username exists, false otherwise
     * @throws SQLException if SQL error occurs
     */
    static boolean usernameExists(final String username) throws SQLException {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM users WHERE username=?)"
            )
        ) {
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        }

        return exists;
    }

    /**
     * Check if an email exists.
     * @param email email
     * @return true if email exists, false otherwise
     * @throws SQLException if SQL error occurs
     */
    static boolean emailExists(final String email) throws SQLException {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM users WHERE email=?)"
            )
        ) {
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        }

        return exists;
    }

    /**
     * Get a user's ID from their username.
     * @param username username
     * @return user ID
     * @throws SQLException if SQL error occurs
     */
    static @Nullable Integer getUserIDFromUsername(
        final String username
    ) throws SQLException {
        Integer uid = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT uid FROM users WHERE username=?"
            )
        ) {
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                uid = rs.getInt("uid");
            }
        }

        return uid;
    }

    /**
     * Get a user's ID from their email.
     * @param email email
     * @return user ID
     * @throws SQLException if SQL error occurs
     */
    static Integer getUserIDFromEmail(final String email) throws SQLException {
        Integer uid = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT UID FROM users WHERE email=?"
            )
        ) {
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                uid = rs.getInt("uid");
            }
        }

        return uid;
    }

    /**
     * Delete a user (by user ID).
     * @param uid user ID
     * @return true if user was deleted, false otherwise
     * @throws SQLException if SQL error occurs
     */
    static boolean deleteUser(final int uid) throws SQLException {
        boolean deleted;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM users WHERE uid=?"
            )
        ) {
            pst.setInt(1, uid);
            deleted = pst.executeUpdate() == 1;
        }
        return deleted;
    }
}
