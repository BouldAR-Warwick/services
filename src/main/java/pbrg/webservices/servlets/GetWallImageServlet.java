package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import static pbrg.webservices.database.WallController
    .getWallIdFromGymId;
import static pbrg.webservices.database.WallController
    .getWallImageFileNameFromWallId;
import static pbrg.webservices.utils.Utils.returnWallImageAsBitmap;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
        // validate request, session, session requires gid
        if (!validatePreconditions(request, response)) {
            return;
        }

        HttpSession session = getSession(request);
        assert session != null;
        int gymId = (int) session.getAttribute("gid");

        // get wall image file name from gym id
        String wallImageFileName;
        try {
            Integer wallId = getWallIdFromGymId(gymId);
            wallImageFileName = getWallImageFileNameFromWallId(wallId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // wall query failed or no wall against gym
        if (wallImageFileName == null) {
            // case gym has no wall! - TODO
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        returnWallImageAsBitmap(response, wallImageFileName);
    }

    private boolean validatePreconditions(
        final HttpServletRequest request, final HttpServletResponse response
    ) {
        String[] requiredAttributes = {"gid"};

        // validate the response
        if (!validateResponse(response)) {
            System.out.println("Invalid response");
            return false;
        }

        // validate the request
        if (!validateRequest(request, response)) {
            return false;
        }

        // validate the session
        HttpSession session = getSession(request);
        return validateSession(session, requiredAttributes, response);
    }

    private boolean validateRequest(
        final HttpServletRequest request, final HttpServletResponse response
    ) {
        // ensure request is not null
        if (request == null) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private boolean validateResponse(
        final HttpServletResponse response
    ) {
        return response != null;
    }

    private boolean validateSession(
        final HttpSession session, final String[] requiredAttributes,
        final HttpServletResponse response
    ) {
        // error if request is unauthorized
        if (session == null) {
            // return unauthorized error message
            try {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        // ensure session has attributes
        List<String> attributes =
            Collections.list(session.getAttributeNames());
        for (String attribute: requiredAttributes) {
            if (!attributes.contains(attribute)) {
                try {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }

        return true;
    }
}
