package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pbrg.webservices.models.Gym;
import pbrg.webservices.models.User;

public class Database {

    public static User sign_in(String username, String password, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("uid"),
                    rs.getString("username")
                );
            }
        }

        return null;
    }

    public static boolean sign_up(String username, String email, String password, Connection connection) throws SQLException {
        insert_user(username, email, password, connection);
        return user_exists(username, connection);
    }

    private static void insert_user(String username, String email, String password, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement("INSERT INTO users (Username, Email, Password) VALUES (?,?,?)")) {
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, password);
            pst.executeUpdate();
        }
    }

    private static boolean user_exists(String username, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE username=?)")) {
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                return rs.getBoolean(1);
            }
        }
        return false;
    }

    public static List<String> get_gyms(String query_word, Connection connection) throws SQLException {
        try (PreparedStatement pst = connection.prepareStatement("SELECT Gymname FROM gyms WHERE GymLocation LIKE ? OR Gymname LIKE ?")) {
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
        try (PreparedStatement pst = connection.prepareStatement("SELECT GID,Gymname FROM gyms WHERE Gymname = ?")) {
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
}
