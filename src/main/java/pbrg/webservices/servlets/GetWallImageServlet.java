package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import pbrg.webservices.utils.ServletUtils;

import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.WallController
    .getWallIdFromGymId;
import static pbrg.webservices.database.WallController
    .getWallImageFileNameFromWallId;
import static pbrg.webservices.database.WallController.gymHasWall;
import static pbrg.webservices.utils.ServletUtils.returnWallImageAsBitmap;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {

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
        // validate request, session, session requires gid
        String gymIdKey = "gid";
        HttpSession session = getSession(request);

        // error if request is unauthorized
        if (session == null) {
            // return unauthorized error message
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session is null"
            );
            return;
        }

        // ensure session has attributes
        boolean sessionHasGid = session.getAttribute(gymIdKey) != null;
        if (!sessionHasGid) {
            // return unauthorized error message
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session has no gid"
            );
            return;
        }

        int gymId = (int) session.getAttribute(gymIdKey);

        // ensure the gym exists
        if (!gymExists(gymId)) {
            // return unauthorized error message
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Gym does not exist"
            );
            return;
        }

        // ensure the gym has a wall
        if (!gymHasWall(gymId)) {
            // return unauthorized error message
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Gym has no wall"
            );
            return;
        }

        // ensure the wall has an image
        Integer wallId = getWallIdFromGymId(gymId);
        assert wallId != null;
        String wallImageFileName = getWallImageFileNameFromWallId(wallId);
        if (wallImageFileName == null) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Image file name is null, but wall exists in gym"
            );
            return;
        }

        // ensure the file exists
        File wallImageFile = new File(
            ServletUtils.getWallImagePath() + wallImageFileName
        );
        if (!wallImageFile.exists()) {
            response.sendError(
                HttpServletResponse.SC_EXPECTATION_FAILED,
                "Image file does not exist"
            );
            return;
        }

        try {
            returnWallImageAsBitmap(response, wallImageFileName);
        } catch (IOException e) {
            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error reading image file"
            );
        }
    }
}
