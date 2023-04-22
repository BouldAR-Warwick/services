package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController
    .getRoutesInGymMadeByUser;
import static pbrg.webservices.utils.ServletUtils.sessionHasAttributes;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pbrg.webservices.models.Route;

@WebServlet(name = "GetRoutesServlet", urlPatterns = "/GetRoutes")
public class GetRoutesServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ensure session has required attributes: gym id, user id
        String[] requiredSessionAttributes = {"gid", "uid"};
        if (!sessionHasAttributes(session, requiredSessionAttributes)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // collect gym id, user id from cookies
        int gymId = (int) session.getAttribute("gid");
        int userId = (int) session.getAttribute("uid");

        List<Route> routes;
        try {
            routes = getRoutesInGymMadeByUser(gymId, userId);
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
