package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.Nullable;

public final class WallController {

    /** Static class, no need to instantiate. */
    private WallController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create a wall at a gym.
     * @param gymId the gym id
     * @param wallContent the wall content
     * @param imageFileName the image file name
     * @return the wall id
     */
    public static Integer addWall(
        final int gymId,
        final String wallContent,
        final String imageFileName
    ) {
        Integer wallId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO walls (gid, wallContent, image_file_name) "
                    + "VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            Object[] values = {gymId, wallContent, imageFileName};
            String[] types = {"int", "String", "String"};

            for (int i = 0; i < values.length; i++) {
                Object value = values[i];
                String type = types[i];

                if (type.equals("String")) {
                    pst.setString(i + 1, (String) value);
                } else {
                    pst.setInt(i + 1, (int) value);
                }
            }

            // get keys
            pst.executeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                wallId = rs.getInt(1);
            }
        } catch (SQLException e) {
            return null;
        }
        return wallId;
    }

    /**
     * Get a wall image file name by wall id.
     * @param wallId the wall id
     * @return the wall image file name
     */
    public static @Nullable String getWallImageFileName(
        final int wallId
    ) {
        String fileName = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT walls.image_file_name "
                    + "FROM walls "
                    + "WHERE WID = ?"
            )
        ) {
            pst.setInt(1, wallId);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            if (rs.next()) {
                fileName = rs.getString("image_file_name");
            }
        } catch (SQLException e) {
            return null;
        }
        return fileName;
    }

    /**
     * Get a wall ID from a route ID.
     * @param routeId route identifier
     * @return wall identifier, null if an SQL exception occurs
     */
    public static @Nullable Integer getWallIdFromRouteId(
        final int routeId
    ) {
        Integer wallId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT routes.WID "
                    + "FROM routes "
                    + "WHERE RID = ?"
            )
        ) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                wallId = Integer.parseInt(rs.getString("WID"));
            }
        } catch (SQLException e) {
            return null;
        }

        return wallId;
    }

    /**
     * Returns the wall image file name based on the route ID.
     * @param routeId the route ID
     * @return the wall image file name if found, null otherwise
     */
    public static @Nullable String getWallImageFileNameFromRouteId(
        final int routeId
    ) {
        Integer wallId = getWallIdFromRouteId(routeId);
        if (wallId == null) {
            return null;
        }
        return getWallImageFileName(wallId);
    }

    /**
     * Get a gym ID from a gym ID.
     * @param gymId gym identifier
     * @return wall identifier, or null if none found
     */
    public static Integer getWallIdFromGymId(
        final int gymId
    ) {
        Integer wallId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT walls.WID "
                    + "FROM walls "
                    + "WHERE GID = ?"
            )
        ) {
            pst.setInt(1, gymId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                wallId = Integer.parseInt(rs.getString("WID"));
            }
        } catch (SQLException e) {
            return null;
        }
        return wallId;
    }

    /**
     * Check if a gym has a wall.
     * @param gymId the gym id
     * @return true if the gym has a wall, false otherwise
     */
    public static boolean gymHasWall(final int gymId) {
        boolean has = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT walls.WID "
                    + "FROM walls "
                    + "WHERE GID = ?)"
            )
        ) {
            pst.setInt(1, gymId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                has = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
        return has;
    }

    /**
     * Check if a route has a wall.
     * @param routeId the route id
     * @return true if the route has a wall, false otherwise
     */
    public static boolean routeHasWall(final int routeId) {
        boolean has = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT routes.WID "
                    + "FROM routes "
                    + "WHERE RID = ?)"
            )
        ) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                has = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
        return has;
    }

    /**
     * Check if a wall exists by id.
     * @param wallId the wall id
     * @return true if the wall exists, false otherwise
     */
    public static boolean wallExists(final int wallId) {
        boolean exists = false;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 "
                    + "FROM walls "
                    + "WHERE WID = ?)"
            )
        ) {
            pst.setInt(1, wallId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                exists = rs.getBoolean(1);
            }
        } catch (SQLException e) {
            return false;
        }
        return exists;
    }

    /**
     * Delete a wall by wall ID.
     * @param wallId the wall id
     * @return true if the wall was deleted, false otherwise
     */
    public static boolean deleteWall(final int wallId) {
        boolean removed;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM walls WHERE WID = ?"
            )
        ) {
            pst.setInt(1, wallId);
            removed = pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
        return removed;
    }

    /**
     * Get the wall ID from a route ID.
     * @param routeId the route ID
     * @return the wall ID, or null if none found
     */
    public static Integer getWallIdFromRoute(
        final int routeId
    ) {
        Integer wallId = null;
        try (
            Connection connection = getDataSource().getConnection();
            PreparedStatement pst = connection.prepareStatement(
                "SELECT WID FROM routes WHERE RID = ?"
            )
        ) {
            pst.setInt(1, routeId);
            ResultSet rs = pst.executeQuery();

            // get JSON list of holds
            if (rs.next()) {
                wallId = rs.getInt("WID");
            }
        } catch (SQLException e) {
            return null;
        }
        return wallId;

    }
}
