package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import pbrg.webservices.Singleton;
import pbrg.webservices.models.Gym;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetPrimaryGymServlet", urlPatterns = "/GetPrimaryGym")
public class GetPrimaryGymServlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int user_id = (int) session.getAttribute("uid");

        Gym gym = null;
        try {
            Connection connection = Singleton.getDbConnection();
            gym = Database.get_gym_by_user_id(user_id, connection);
            Singleton.closeDbConnection();
        } catch (SQLException exception) {
            response.getWriter().println(exception.getMessage());
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // when no gyms are matched
        if (gym == null) {
            response.getWriter().write("{}");
            return;
        }

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);
    }
}
