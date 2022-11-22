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

@WebServlet(name = "RegisterServlet", urlPatterns = "/Register")
public class RegisterServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jObj = new JSONObject(getBody(request));
        Iterator<String> it = jObj.keys();

        String username = jObj.getString("username");
        String pwd = jObj.getString("password");
        String email = jObj.getString("email");
        PrintWriter out = response.getWriter();

        try
        {   
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/grabourg");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("INSERT INTO USERS (Username, Email, Password) VALUES (?,?,?)");
            pst.setString(1, username);
            pst.setString(2, email);
            pst.setString(3, pwd);
            pst.executeUpdate();
            pst.close();

            pst = conn.prepareStatement("SELECT * FROM USERS WHERE username=?");
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            LoggedInUser user = new LoggedInUser(0,null);
            if(rs.next()) {
                user = new LoggedInUser(rs.getInt("uid"),rs.getString("username"));
                HttpSession session = request.getSession();
                session.setAttribute("uid",rs.getInt("uid"));
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
