package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pbrg.webservices.Singleton;
import pbrg.webservices.models.Gym;
import pbrg.webservices.models.Route;
import pbrg.webservices.models.User;

public class Database {

    public static User sign_in(String username, String password, Connection connection)
        throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("uid"), rs.getString("username"));
            }
        }

        return null;
    }

    public static boolean sign_up(
        String username, String email, String password, Connection connection) throws SQLException {
        insert_user(username, email, password, connection);
        return user_exists(username, connection);
    }

    private static void insert_user(
        String username, String email, String password, Connection connection) throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement(
                "INSERT INTO users (Username, Email, Password) VALUES (?,?,?)")) {
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, password);
            pst.executeUpdate();
        }
    }

    private static boolean user_exists(String username, Connection connection) throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE username=?)")) {
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        }
        return false;
    }

    public static List<String> get_gyms(String query_word, Connection connection)
        throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement(
                "SELECT Gymname FROM gyms WHERE GymLocation LIKE ? OR Gymname LIKE ?")) {
            pst.setString(1, "%" + query_word + "%");
            pst.setString(2, "%" + query_word + "%");
            ResultSet rs = pst.executeQuery();

            List<String> gyms = new ArrayList<>();
            while (rs.next()) {
                gyms.add(rs.getString("Gymname"));
            }
            return gyms;
        }
    }

    public static Gym get_gym_by_name(String gym_name, Connection connection) throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement("SELECT GID,Gymname FROM gyms WHERE Gymname = ?")) {
            pst.setString(1, gym_name);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int gid = rs.getInt("GID");
                gym_name = rs.getString("Gymname");
                return new Gym(gid, gym_name);
            }
        }
        return null;
    }

    public static Gym get_gym_by_user_id(int user_id, Connection connection) throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement(
                "SELECT (GID, Gymname) FROM gyms WHERE GID = (SELECT GID FROM user_in_gym WHERE UID = ?)")) {
            pst.setInt(1, user_id);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int gid = rs.getInt("GID");
                String gym_name = rs.getString("Gymname");
                return new Gym(gid, gym_name);
            }
        }

        return null;
    }

    public static List<String> get_route_ids_by_gym_id(int gym_id, Connection connection)
        throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement(
                "SELECT routes.RID "
                    + "FROM routes "
                    + "INNER JOIN walls ON routes.WID = walls.WID "
                    + "INNER JOIN gyms ON walls.GID = gyms.GID "
                    + "WHERE gyms.GID = ?")) {
            pst.setInt(1, gym_id);

            ResultSet rs = pst.executeQuery();
            List<String> route_ids = new ArrayList<>();
            while (rs.next()) {
                route_ids.add(rs.getString("RID"));
            }
            return route_ids;
        }
    }

    public static String get_wall_image_file_name_from_gym_id(int gym_id, Connection connection)
        throws SQLException {
        try (PreparedStatement pst =
            connection.prepareStatement(
                "SELECT (walls.image_file_name) " + "FROM walls " + "WHERE GID = ?")) {
            pst.setInt(1, gym_id);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            if (rs.next()) {
                return rs.getString("image_file_name");
            }
        }

        return null;
    }

    private static Route get_route_by_route_id(int route_id)
        throws SQLException {
        try (Connection connection = Singleton.getDbConnection()) {
            assert connection != null;
            try (PreparedStatement pst =
                connection.prepareStatement(
                    "SELECT * "
                        + "FROM routes "
                        + "WHERE routes.RID = ?")) {
                pst.setInt(1, route_id);

                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    return new Route(
                        rs.getInt("RID"),
                        rs.getInt("WID"),
                        rs.getInt("creator_user_id"),
                        rs.getInt("Difficulty"),
                        rs.getString("RouteContent"),
                        rs.getString("RouteContent")
                    );
                }
            }
        }

        return null;
    }

    public static String get_route_image_file_names_by_route_id(int route_id) throws SQLException {
        Route route = get_route_by_route_id(route_id);

        if (route == null) {
            return null;
        }

        return route.getImage_file_name();
    }
}
