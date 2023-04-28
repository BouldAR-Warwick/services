package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController.routeExists;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import pbrg.webservices.utils.RouteUtils;

@WebServlet(name = "DeleteRouteServlet", urlPatterns = "/DeleteRoute")
public class DeleteRouteServlet extends MyHttpServlet {

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

        // get parameters
        HttpSession session = getSession(request);
        assert session != null;
        int routeId = (int) session.getAttribute(routeIdKey);

        // ensure the route exists
        if (!routeExists(routeId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route does not exist"
            );
            return;
        }

        // delete the route (removes route image if generated)
        RouteUtils.deleteRoute(routeId);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
