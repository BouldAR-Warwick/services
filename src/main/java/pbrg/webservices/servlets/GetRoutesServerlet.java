package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.json.JSONObject;
import pbrg.webservices.Singleton;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetRoutesServerlet", urlPatterns = "/getRoutes")
public class GetRoutesServerlet extends MyHttpServlet {

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

        // get json object in the request body
        JSONObject parameters = new JSONObject(getBody(request));

        if (!parameters.has("gymID")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int gym_id = (int) parameters.get("gymID");

        List<String> route_ids = null;
        try {
            Connection connection = Singleton.getDbConnection();
            route_ids = Database.get_route_ids_by_gym_id(gym_id, connection);
            Singleton.closeDbConnection();
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // query for routes failed
        if (route_ids == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String[] arrayOfRouteIDs = route_ids.toArray(new String[0]);
        String json = new Gson().toJson(arrayOfRouteIDs);
        response.getWriter().write(json);
    }
}
