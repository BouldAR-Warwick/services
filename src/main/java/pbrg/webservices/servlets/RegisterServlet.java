package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;
import java.io.*;

import pbrg.webservices.Singleton;
import pbrg.webservices.models.User;
import pbrg.webservices.utils.Database;

@WebServlet(name = "RegisterServlet", urlPatterns = "/Register")
public class RegisterServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // parse credentials
        JSONObject credentials;
        try {
            credentials = new JSONObject(getBody(request));
            // Iterator<String> it = credentials.keys();
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // ensure request has all credentials
        String[] requiredCredentials = {"username", "email", "password"};
        if (!Arrays.stream(requiredCredentials).allMatch(credentials::has)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // store credentials
        String username = credentials.getString("username");
        String email = credentials.getString("email");
        String password = credentials.getString("password");

        // create new user
        boolean added = false;
        try {
            Connection connection = Singleton.getDbConnection();
            added = Database.sign_up(username, email, password, connection);
            Singleton.closeDbConnection();
        } catch(SQLException e){
            response.getWriter().println(e.getMessage());
        }

        // ensure user was added
        if (!added) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // select user
        User user = null;
        try {
            Connection connection = Singleton.getDbConnection();
            user = Database.sign_in(username, password, connection);
            Singleton.closeDbConnection();
        } catch(Exception e){
            response.getWriter().println(e.getMessage());
        }

        // case one -> the user has not been authenticated (wrong credentials)
        boolean userLoggedIn = (user != null);
        if (!userLoggedIn) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // case two -> user is authenticated

        HttpSession session = request.getSession();
        session.setAttribute("uid", user.get_uid());

        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
