package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.*;
import java.io.*;
import java.net.URLDecoder;

import pbrg.webservices.models.LoggedInUser;

@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // get json object in the request body
        JSONObject jObj = new JSONObject(getBody(request));
        String username = jObj.getString("username");
        String pwd = jObj.getString("password");
        Boolean stayLoggedIn = jObj.getBoolean("stayLoggedIn");

        PrintWriter out = response.getWriter();

        try
        {
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/grabourg");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM users WHERE username=?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            LoggedInUser user = new LoggedInUser(0,null);
            if(rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("uid",rs.getInt("uid"));
                if (pwd.equals(rs.getString("password"))) {
                    user = new LoggedInUser(rs.getInt("uid"),rs.getString("username"));
                    if (stayLoggedIn) {
                        // create cookie and store logged in user info in cookie
                        Cookie cookie1 = new Cookie("username", rs.getString("username"));
                        Cookie cookie2 = new Cookie("uid", String.valueOf(rs.getInt("uid")));
                        // set expired time to 7 days
                        cookie1.setMaxAge(302400); 
                        cookie2.setMaxAge(302400);
                        // send cookie back to client for authentication next time
                        response.addCookie(cookie1);
                        response.addCookie(cookie2);
                    }
                }
            }
            
            String json = new Gson().toJson(user);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            pst.close();
            conn.close();
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }
    }
}
