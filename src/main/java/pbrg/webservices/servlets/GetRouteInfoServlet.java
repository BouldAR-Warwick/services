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
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return;
        }

        // ensure route is stored in session
        String routeIdKey = "rid";
        boolean sessionHasRouteId = session.getAttribute(routeIdKey) != null;
        if (!sessionHasRouteId) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Session does not have a route id"
            );
            return;
        }

        // ensure the route exists
        int routeId = (int) session.getAttribute(routeIdKey);
        if (!routeExists(routeId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Route does not exist"
            );
            return;
        }

        JSONArray listOfHolds = getRouteContentJSONArray(routeId);
        assert listOfHolds != null;

        // nest the list of holds in the response body
        JSONObject responseBody = new JSONObject();
        responseBody.put("info", listOfHolds);

        // return the list of holds
        response.getWriter().println(responseBody);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
