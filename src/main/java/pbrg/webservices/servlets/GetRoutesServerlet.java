package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import pbrg.webservices.models.Route;
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

        // ensure session has required attributes: gym id, user id
        if (session.getAttribute("gid") == null|| session.getAttribute("uid") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // collect gym id, user id from cookies
        int gymId = (int) session.getAttribute("gid");
        int userId = (int) session.getAttribute("uid");

        List<Route> routes;
        try {
            routes = Database.getRoutesInGymMadeByUser(gymId, userId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Route[] arrayOfRoutes = routes.toArray(new Route[0]);
        String json = new Gson().toJson(arrayOfRoutes);
        response.getWriter().write(json);
    }
}
