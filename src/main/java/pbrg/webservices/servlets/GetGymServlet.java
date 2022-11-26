package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import org.json.JSONObject;
import pbrg.webservices.Singleton;
import pbrg.webservices.models.Gym;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetGymServlet", urlPatterns = "/GetGym")
public class GetGymServlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

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

        Gym gym = null;
        try {
            Connection connection = Singleton.getDbConnection();
            assert connection != null;
            gym = Database.get_gym_by_name(gym_name, connection);
            connection.close();
        } catch (Exception e) {
            response.getWriter().println(e.getMessage());
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
