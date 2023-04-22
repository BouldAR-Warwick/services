package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController
    .getRouteImageFileNamesByRouteId;
import static pbrg.webservices.utils.ServletUtils.returnRouteImageAsBitmap;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

@WebServlet(name = "GetRouteImageServlet", urlPatterns = "/GetRouteImage")
public class GetRouteImageServlet extends MyHttpServlet {

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

        // ensure session has route_id
        if (session.getAttribute("rid") == null) {
            // no route id in session
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        Integer routeId = (Integer) session.getAttribute("rid");

        // get the route image file name
        String routeImageFileName;
        try {
            routeImageFileName = getRouteImageFileNamesByRouteId(routeId);
        } catch (SQLException exception) {
            response.getWriter().println(exception.getMessage());
            return;
        }

        // route does not exist
        if (routeImageFileName == null) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        returnRouteImageAsBitmap(response, routeImageFileName);
    }
}
