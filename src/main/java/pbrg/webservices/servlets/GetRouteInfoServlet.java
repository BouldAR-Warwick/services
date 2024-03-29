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
        // validate request
        boolean requiresSession = true;
        String routeIdKey = "rid";
        String[] sessionAttributes = {routeIdKey};
        String[] bodyAttributes = {};
        if (!validateRequest(
            request, response, null, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // get route id
        HttpSession session = getSession(request);
        assert session != null;
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

        // nest the list of holds in the response body
        JSONObject responseBody = new JSONObject();
        responseBody.put("info", listOfHolds);

        // return the list of holds
        response.getWriter().println(responseBody);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
