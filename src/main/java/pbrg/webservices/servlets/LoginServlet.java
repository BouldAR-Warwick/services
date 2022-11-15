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

        if (!validateLogin(request)) {
            return;
        }

        int uid = Integer.parseInt(request.getParameter("uid"));
        String pwd = request.getParameter("pwd");

        PrintWriter out = response.getWriter();
//        out.println("username:"+uid+"password:"+pwd);

        try
        {
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/test");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT * FROM test.\"Users\";");
            ResultSet rs = pst.executeQuery();
            StringBuilder table = new StringBuilder();
            table.append("<table border='1'>");
            table.append("<tr><td>uid</td><td>username</td><td>password</td><td>email</td></tr>");
            while(rs.next())
            {
                table.append("<tr><td>"+rs.getInt("uid")+"</td><td>");
                table.append(rs.getString("username")+"</td><td>");
                table.append(rs.getString("password")+"</td><td>");
                table.append(rs.getString("email")+"</td></tr>");
            }
            table.append("</table>");
            out.println(table.toString());
            pst.close();
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }


    }
}
