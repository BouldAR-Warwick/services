package pbrg.webservices.servlets;

import static pbrg.webservices.utils.RouteUtils.getRouteContentJSONArray;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "GetRouteInfoServlet", urlPatterns = "/GetRouteInfo")
public class GetRouteInfoServlet extends MyHttpServlet {

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

        // ensure route is stored in session
        if (session.getAttribute("rid") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // collect gym id, user id from cookies
        int routeId = (int) session.getAttribute("rid");

        JSONArray listOfHolds;
        try {
            listOfHolds = getRouteContentJSONArray(routeId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // return: nest the hold array in a JSON object under key info
        JSONObject info = new JSONObject();
        info.put("info", listOfHolds);
        response.getWriter().println(info);
    }
}
