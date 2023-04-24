package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileNameByRouteId;
import static pbrg.webservices.utils.ServletUtils.returnRouteImageAsBitmap;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import pbrg.webservices.utils.ServletUtils;

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
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return;
        }

        // ensure session has the route id
        String routeIdKey = "rid";
        boolean sessionWithoutRouteId =
            session.getAttribute(routeIdKey) == null;
        if (sessionWithoutRouteId) {
            // no route id in session
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Session has no route id"
            );
            return;
        }

        int routeId = (int) session.getAttribute(routeIdKey);

        // ensure the route exists
        if (!routeExists(routeId)) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route does not exist"
            );
            return;
        }

        // get the route image file name
        String routeImageFileName = getRouteImageFileNameByRouteId(routeId);

        // ensure the route image has been generated
        if (routeImageFileName == null) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route image has not been generated"
            );
            return;
        }

        // ensure the file exists
        File wallImageFile = new File(
            ServletUtils.getRouteImagePath(), routeImageFileName
        );
        if (!wallImageFile.exists()) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Image file does not exist"
            );
            return;
        }

        try {
            returnRouteImageAsBitmap(response, routeImageFileName);
        } catch (IOException e) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error reading image file"
            );
        }

        // send ok response
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
