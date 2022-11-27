package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.json.JSONObject;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetRoutesServerlet", urlPatterns = "/GetRoutes")
public class GetRoutesServerlet extends MyHttpServlet {

    @Override
    protected final void doGet(
            final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
            final HttpServletRequest request, final HttpServletResponse response) throws IOException {

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

        int gymId = (int) parameters.get("gymID");

        List<String> routeIds;
        try {
            routeIds = Database.getRouteIdsByGymId(gymId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String[] arrayOfRouteIDs = routeIds.toArray(new String[0]);
        String json = new Gson().toJson(arrayOfRouteIDs);
        response.getWriter().write(json);
    }
}
