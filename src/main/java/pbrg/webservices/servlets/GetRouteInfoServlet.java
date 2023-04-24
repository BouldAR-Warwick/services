package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.utils.RouteUtils.getRouteContentJSONArray;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
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
        String routeIdKey = "rid";
        boolean sessionWithoutRouteId =
            session.getAttribute(routeIdKey) == null;
        if (sessionWithoutRouteId) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // collect gym id, user id from cookies
        int routeId = (int) session.getAttribute(routeIdKey);

        // ensure the route exists
        if (!routeExists(routeId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Route does not exist"
            );
            return;
        }

        JSONArray listOfHolds = getRouteContentJSONArray(routeId);
        assert listOfHolds != null;

        // return: nest the hold array in a JSON object under key info
        JSONObject info = new JSONObject();
        info.put("info", listOfHolds);
        response.getWriter().println(info);
    }
}
