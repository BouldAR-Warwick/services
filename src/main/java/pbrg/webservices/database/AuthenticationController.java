package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.Nullable;
import pbrg.webservices.models.User;

public final class AuthenticationController {

    /** Static class, no need to instantiate. */
    private AuthenticationController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sign a user in.
     *
     * @param username username
     * @param password password
     * @return user object
     */
    public static @Nullable User signIn(
        final String username, final String password) {
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
        } catch (SQLException e) {
            return null;
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
     */
    public static boolean signUp(
        final String username, final String email, final String password
    ) {
        Integer userId = addUser(username, email, password);
        boolean added = userId != null;
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
     * @return user id if user was added, null otherwise
     */
    public static @Nullable Integer addUser(
        final String username, final String email, final String password
    ) {
        // ensure username, email are unique
        if (usernameExists(username) || emailExists(email)) {
            return null;
        }

        Integer userId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO users (Username, Email, Password) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            String[] values = {username, email, password};
            for (int i = 1; i <= values.length; i++) {
                pst.setString(i, values[i - 1]);
            }
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                userId = rs.getInt(1);
            }
        } catch (SQLException e) {
            return usernameExists(username)
                ? getUserIDFromUsername(username) : null;
        }
        return userId;
    }

    /**
     * Check if a user exists by id.
     * @param uid user id
     * @return true if user exists, false otherwise
     */
    public static boolean userExists(final int uid) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM users WHERE uid=?)"
            )
        ) {
            pst.setInt(1, uid);
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
     * Check if a username exists.
     * @param username username
     * @return true if username exists, false otherwise
     */
    static boolean usernameExists(final String username) {
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
        } catch (SQLException e) {
            return false;
        }
        return exists;
    }

    /**
     * Check if an email exists.
     * @param email email
     * @return true if email exists, false otherwise
     */
    static boolean emailExists(final String email) {
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
        } catch (SQLException e) {
            return false;
        }
        return exists;
    }

    /**
     * Get a user's ID from their username.
     * @param username username
     * @return user ID
     */
    public static @Nullable Integer getUserIDFromUsername(
        final String username
    ) {
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
        } catch (SQLException e) {
            return null;
        }
        return uid;
    }

    /**
     * Get a user's ID from their email.
     * @param email email
     * @return user ID
     */
    static Integer getUserIDFromEmail(final String email) {
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
        } catch (SQLException e) {
            return null;
        }
        return uid;
    }

    /**
     * Delete a user (by user ID).
     * @param uid user ID
     * @return true if user was deleted, false otherwise
     */
    public static boolean deleteUser(final int uid) {
        boolean deleted;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM users WHERE uid=?"
            )
        ) {
            pst.setInt(1, uid);
            deleted = pst.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
        return deleted;
    }
}
