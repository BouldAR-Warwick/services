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
import pbrg.webservices.models.Gym;

public final class GymController {

    /** The gym name length cap. */
    static final int GYM_NAME_LENGTH_CAP = 64;

    /** The gym location length cap. */
    static final int GYM_LOCATION_LENGTH_CAP = 32;

    /** Static class, no need to instantiate. */
    private GymController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Insert a gym into the database.
     * @param gymName gym name
     * @param gymLocation gym location
     * @return the gym id if successful, null otherwise
     */
    public static @Nullable Integer addGym(
        final @NotNull String gymName,
        final @NotNull String gymLocation
    ) {
        if (gymName.length() > GYM_NAME_LENGTH_CAP) {
            return null;
        }

        if (gymLocation.length() > GYM_LOCATION_LENGTH_CAP) {
            return null;
        }

        if (gymExists(gymName)) {
            return null;
        }

        return insertGym(gymName, gymLocation);
    }

    /**
     * Delete a gym from the database.
     * @param gymName gym name
     * @param gymLocation gym location
     * @return the gym id if successful, null otherwise
     */
    private static @Nullable Integer insertGym(
        final String gymName, final String gymLocation
    ) {
        Integer gymId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO gyms (GymName, GymLocation) VALUES (?,?)",
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            String[] values = {gymName, gymLocation};
            for (int i = 1; i <= values.length; i++) {
                pst.setString(i, values[i - 1]);
            }
            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                gymId = rs.getInt(1);
            }
        } catch (SQLException e) {
            return null;
        }
        return gymId;
    }

    /**
     * Check if a gym exists (by gym name).
     * @param gymId gym identifier
     * @return true if gym exists, false otherwise
     */
    public static boolean gymExists(final int gymId) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM gyms WHERE GID=?)"
            )
        ) {
            pst.setInt(1, gymId);

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
     * Check if a gym exists (by gym name).
     * @param gymName gym name
     * @return true if gym exists, false otherwise
     */
    public static boolean gymExists(final String gymName) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM gyms WHERE GymName=?)"
            )
        ) {
            pst.setString(1, gymName);

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
     * Delete a gym (by gym ID).
     * @param gymID gym ID
     * @return true if gym was deleted, false otherwise
     */
    public static boolean deleteGym(
        final int gymID
    ) {
        boolean deleted;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM gyms WHERE GID=?"
            )
        ) {
            pst.setInt(1, gymID);
            deleted = pst.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
        return deleted;
    }

    /**
     * Get all gyms matching a query word in its location or name.
     * @param queryWord query word
     * @return list of gyms
     */
    public static @NotNull List<String> getGymsByQueryWord(
        final @NotNull String queryWord
    ) {
        List<String> gyms = new ArrayList<>();
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GymName "
                    + "FROM gyms "
                    + "WHERE GymLocation LIKE ? OR GymName LIKE ?"
            )
        ) {
            pst.setString(1, "%" + queryWord + "%");
            pst.setString(2, "%" + queryWord + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                gyms.add(rs.getString("GymName"));
            }
        } catch (SQLException e) {
            return gyms;
        }
        return gyms;
    }

    /**
     * Find a gym by its name.
     * @param gymName The name of the gym.
     * @return The gym.
     */
    public static @Nullable Gym getGymByGymName(
        final @NotNull String gymName
    ) {
        Gym gym;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID, GymName, GymLocation FROM gyms WHERE Gymname = ?")
        ) {
            pst.setString(1, gymName);
            gym = extractGym(pst);
        } catch (SQLException e) {
            return null;
        }
        return gym;
    }

    /**
     * Find a gym by its id.
     * @param gymId The id of the gym.
     * @return The gym.
     */
    public static @Nullable Gym getGym(final int gymId) {
        Gym gym;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID, GymName, GymLocation FROM gyms WHERE GID = ?")
        ) {
            pst.setInt(1, gymId);
            gym = extractGym(pst);
        } catch (SQLException e) {
            return null;
        }
        return gym;
    }

    /**
     * Extract a Gym from a result set.
     * @param pst prepared statement
     * @return gym
     * @throws SQLException if database error
     */
    private static @Nullable Gym extractGym(
        final @NotNull PreparedStatement pst
    ) throws SQLException {
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            int gid = rs.getInt("GID");
            String rGymName = rs.getString("GymName");
            String gymLocation = rs.getString("GymLocation");
            return new Gym(gid, rGymName, gymLocation);
        }
        return null;
    }

    /**
     * Set the primary gym for a user.
     * @param userId user ID
     * @param gymId gym ID
     * @return true if added, false otherwise
     */
    public static boolean setPrimaryGym(
        final int userId, final int gymId
    ) {
        boolean added;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO user_in_gym (UID, GID) VALUES (?, ?)"
            )
        ) {
            pst.setInt(1, userId);
            pst.setInt(2, gymId);
            added = pst.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
        return added;
    }

    /**
     * Remove a user's primary gym.
     * @param userId user ID
     * @return true if removed, false otherwise
     */
    public static boolean removeUserPrimaryGym(
        final int userId
    ) {
        boolean removed;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM user_in_gym WHERE UID = ?"
            )
        ) {
            pst.setInt(1, userId);
            removed = pst.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
        return removed;
    }

    /**
     * Get a user's primary gym id.
     * @param userId user ID
     * @return a gym ID
     */
    public static @Nullable Integer getPrimaryGymOfUser(final int userId) {
        Integer gymId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID FROM user_in_gym WHERE UID = ?"
            )
        ) {
            pst.setInt(1, userId);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                gymId = rs.getInt("GID");
            }
        } catch (SQLException e) {
            return null;
        }
        return gymId;
    }

    /**
     * Get a gym id from the gym name.
     * @param gymName gym name
     * @return gym id
     */
    public static @Nullable Integer getGymIdByGymName(
        final @NotNull String gymName
    ) {
        Integer gymId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT GID FROM gyms WHERE GymName = ?"
            )
        ) {
            pst.setString(1, gymName);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                gymId = rs.getInt("GID");
            }
        } catch (SQLException e) {
            return null;
        }
        return gymId;
    }

    /**
     * Check if a user has a primary gym.
     * @param userId user ID
     * @return true if user has a primary gym, false otherwise
     */
    public static boolean userHasPrimaryGym(
        final int userId
    ) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS(SELECT 1 FROM user_in_gym WHERE UID = ?)"
            )
        ) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
        return exists;
    }
}
