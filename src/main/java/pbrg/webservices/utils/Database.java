package pbrg.webservices.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import pbrg.webservices.models.User;

public class Database {

    public static User sign_in(String username, String password, Connection connection) throws SQLException {
        PreparedStatement pst = null;
        User user = null;

        try {
            pst = connection.prepareStatement(
                "SELECT * FROM users WHERE username=? AND password=?"
            );
            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();
            
            if(rs.next()) {
                user = new User(
                    rs.getInt("uid"),
                    rs.getString("username")
                );
            }
        } catch (SQLException ignore) {
        } finally {
            if (pst != null) {
                pst.close();
            }
        }
        
        return user;
    }

    public static boolean sign_up(String username, String email, String password, Connection connection) throws SQLException {
        insert_user(username, email, password, connection);

        return user_exists(username, connection);
    }

    private static void insert_user(String username, String email, String password, Connection connection) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement("INSERT INTO users (Username, Email, Password) VALUES (?,?,?)");
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, password);
            pst.executeUpdate();
        } catch (SQLException ignore) {
        } finally {
            if (pst != null) {
                pst.close();
            }
        }
    }

    private static boolean user_exists(String username, Connection connection) throws SQLException {
        PreparedStatement pst = null;
        boolean exists = false;

        try {
            pst = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE username=?)");
            pst.setString(1, username);

            ResultSet rs = pst.executeQuery();
            if(rs.next()){
                exists = rs.getBoolean(1);
            }
        } catch (SQLException ignore) {
        } finally {
            if (pst != null) {
                pst.close();
            }
        }

        return exists;
    }
}
