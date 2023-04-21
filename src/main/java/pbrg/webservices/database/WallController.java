package pbrg.webservices.database;

import static pbrg.webservices.database.DatabaseController.getDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

public final class WallController {

    /** Static class, no need to instantiate. */
    private WallController() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get a wall image file name by wall id.
     * @param wallId the wall id
     * @return the wall image file name
     * @throws SQLException If the query fails
     */
    public static String getWallImageFileNameFromWallId(
        final int wallId
    ) throws SQLException {
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
        }

        return fileName;
    }

    /**
     * Get a wall ID from a route ID.
     * @param routeId route identifier
     * @return wall identifier
     * @throws SQLException database issues
     */
    public static @Nullable Integer getWallIdFromRouteId(
        final int routeId
    ) throws SQLException {
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
        }

        return wallId;
    }

    /**
     * Returns the wall image file name based on the route ID.
     * @param routeId the route ID
     * @return the wall image file name if found, null otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static @Nullable String getWallImageFileNameFromRouteId(
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
        }

        return wallId;
    }
}
