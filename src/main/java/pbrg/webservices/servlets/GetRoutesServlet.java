package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;

import pbrg.webservices.models.Route;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.Utils;

@WebServlet(name = "GetRoutesServlet", urlPatterns = "/GetRoutes")
public class GetRoutesServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
            final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ensure session has required attributes: gym id, user id
        String[] requiredSessionAttributes = {"gid", "uid"};
        if (!Utils.sessionHasAttributes(session, requiredSessionAttributes)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // collect gym id, user id from cookies
        int gymId = (int) session.getAttribute("gid");
        int userId = (int) session.getAttribute("uid");

        List<Route> routes;
        try {
            routes = DatabaseController.getRoutesInGymMadeByUser(gymId, userId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonObject = new JSONObject();

        Route[] arrayOfRoutes = routes.toArray(new Route[0]);

        jsonObject.put("routes", arrayOfRoutes);

        response.getWriter().write(jsonObject.toString());
    }
}
