package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

import org.json.JSONObject;

import pbrg.webservices.utils.DatabaseController;

@WebServlet(name = "GetRouteInfoServlet", urlPatterns = "/GetRouteInfo")
public class GetRouteInfoServlet extends MyHttpServlet {

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

        // ensure route is stored in session
        if (session.getAttribute("rid") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // collect gym id, user id from cookies
        int routeId = (int) session.getAttribute("rid");

        JSONObject listOfHolds;
        try {
            listOfHolds = DatabaseController.getRouteContentJSONObject(routeId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // write list of holds as JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(listOfHolds.toString());
    }
}
