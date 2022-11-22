package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import pbrg.webservices.models.Gym;

@WebServlet(name = "GetPrimaryGymServlet", urlPatterns = "/GetPrimaryGym")
public class GetPrimaryGymServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session==null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        PrintWriter out = response.getWriter();

        try
        {
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/grabourg");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT (GID, Gymname) FROM GYMS WHERE GID = (SELECT GID FROM USER_GYM WHERE UID = ?)");
            pst.setInt(1, (int)session.getAttribute("uid"));
            ResultSet rs = pst.executeQuery();

            int gid = 0;
            String gymname = "";
            Gym gym = new Gym(gid,gymname);
            if(rs.next()) {
                gid = rs.getInt("GID");
                gymname = rs.getString("Gymname");
                gym = new Gym(gid,gymname);
                session.setAttribute("gid",gid);
            }

            String json = new Gson().toJson(gym);
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
