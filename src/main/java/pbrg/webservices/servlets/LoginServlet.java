package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

import pbrg.webservices.servlets.models.LoggedInUser;

@WebServlet(name = "LoginServlet", urlPatterns = "/Login")
public class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    private boolean validateLogin(HttpServletRequest request) {
        if (request == null) {
            System.out.println("request empty");
            return false;
        }

        if (request.getParameter("uid") == null) {
            System.out.println("uid empty");
            return false;
        }

        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JSONObject jObj = new JSONObject(getBody(request));

        Iterator<String> it = jObj.keys();

        while(it.hasNext())
        {
            String key = it.next(); // get key
            Object o = jObj.get(key); // get value
        }

        String uid = jObj.getString("username");
        String pwd = jObj.getString("password");

        PrintWriter out = response.getWriter();
        LoggedInUser user = new LoggedInUser(123,"123456","Tian");
        String json = new Gson().toJson(user);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    //  out.println("username:"+uid+"password:"+pwd);

        // try
        // {
        //     // get a database connection from connection pool
        //     Context ctx = new InitialContext();
        //     DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/test");
        //     Connection conn = ds.getConnection();
        //     PreparedStatement pst = conn.prepareStatement("SELECT * FROM test.\"Users\";");
        //     ResultSet rs = pst.executeQuery();
        //     StringBuilder table = new StringBuilder();
        //     table.append("<table border='1'>");
        //     table.append("<tr><td>uid</td><td>username</td><td>password</td><td>email</td></tr>");
        //     while(rs.next())
        //     {
        //         table.append("<tr><td>"+rs.getInt("uid")+"</td><td>");
        //         table.append(rs.getString("username")+"</td><td>");
        //         table.append(rs.getString("password")+"</td><td>");
        //         table.append(rs.getString("email")+"</td></tr>");
        //     }
        //     table.append("</table>");
        //     out.println(table.toString());
        //     pst.close();
        // }
        // catch(Exception e)
        // {
        //     out.println(e.getMessage());
        // }


    }

    public static String getBody(HttpServletRequest request)  {

    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
        InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } else {
            stringBuilder.append("");
        }
    } catch (IOException ex) {
        // throw ex;
        return "";
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ex) {

            }
        }
    }

    body = stringBuilder.toString();
    return body;
    }
}
