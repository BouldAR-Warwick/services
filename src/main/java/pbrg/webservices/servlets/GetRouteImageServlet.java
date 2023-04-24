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
        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ensure session has the route id
        boolean sessionWithoutRouteId = session.getAttribute("rid") == null;
        if (sessionWithoutRouteId) {
            // no route id in session
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Session has no route id"
            );
            return;
        }

        int routeId = (int) session.getAttribute("rid");

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
        assert routeImageFileName != null;

        // ensure the file exists
        File wallImageFile = new File(
            ServletUtils.getRouteImagePath() + routeImageFileName
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
    }
}
