package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import pbrg.webservices.Singleton;
import pbrg.webservices.models.Gym;

@WebServlet(name = "GetPrimaryGymServlet", urlPatterns = "/GetPrimaryGym")
public class GetPrimaryGymServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session==null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        PrintWriter out = response.getWriter();

        try
        {
            Connection connection = Singleton.getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                    "SELECT (GID, Gymname) FROM gyms WHERE GID = (SELECT GID FROM user_in_gym WHERE UID = ?)"
            );
            pst.setInt(1, (int)session.getAttribute("uid"));
            ResultSet rs = pst.executeQuery();

            int gid = 0;
            String gym_name = "";
            Gym gym = new Gym(gid, gym_name);
            if(rs.next()) {
                gid = rs.getInt("GID");
                gym_name = rs.getString("Gymname");
                gym = new Gym(gid, gym_name);
                session.setAttribute("gid",gid);
            }

            String json = new Gson().toJson(gym);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            pst.close();
            Singleton.closeDbConnection();
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }
    }
}
