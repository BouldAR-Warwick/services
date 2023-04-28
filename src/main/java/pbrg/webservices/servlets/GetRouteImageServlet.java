package pbrg.webservices.servlets;

import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileName;
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

        // ensure the route image has been generated
        String routeImageFileName = getRouteImageFileName(routeId);
        boolean routeImageNotGenerated = routeImageFileName == null;
        if (routeImageNotGenerated) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Route image has not been generated"
            );
            return;
        }

        // ensure the file exists
        File routeImageFile = new File(
            ServletUtils.getRouteImagePath(), routeImageFileName
        );
        if (!routeImageFile.exists()) {
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
            return;
        }

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
