package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController.deleteRoute;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.utils.RouteUtils.deleteRouteImage;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileNameByRouteId;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

@WebServlet(name = "DeleteRouteServlet", urlPatterns = "/DeleteRouteServlet")
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

        // ensure the route exists
        int routeId = (int) session.getAttribute(routeIdKey);
        if (!routeExists(routeId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route does not exist"
            );
            return;
        }

        // ensure the route image has been generated
        String routeImageFileName = getRouteImageFileNameByRouteId(routeId);
        boolean routeImageGenerated = routeImageFileName != null;

        // remove from database
        boolean removedFromDatabase = deleteRoute(routeId);
        if (!removedFromDatabase) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Failed to remove route from database"
            );
            return;
        }

        // remove route image if it exists
        if (routeImageGenerated) {
            boolean removedRouteImage = deleteRouteImage(routeImageFileName);
            if (!removedRouteImage) {
                response.sendError(
                    HttpServletResponse.SC_EXPECTATION_FAILED,
                    "Failed to remove route image"
                );
                return;
            }
        }

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
