package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import pbrg.webservices.Singleton;
import pbrg.webservices.models.Gym;

@WebServlet(name = "GetGymServlet", urlPatterns = "/GetGym")
public class GetGymServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (request == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // get session or return unauthorized error message
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get json object in the request body
        JSONObject jObj = new JSONObject(getBody(request));
        String gym_name = jObj.getString("gymname");

        try {
            Connection connection = Singleton.getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
                    "SELECT GID,Gymname FROM gyms WHERE Gymname = ?"
            );
            pst.setString(1, gym_name);
            ResultSet rs = pst.executeQuery();

            int gid = 0;
            gym_name = "";
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
        } catch(Exception e) {
            response.getWriter().println(e.getMessage());
        }
    }
}

