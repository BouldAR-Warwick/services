package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

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

        // get the gym id from the session
        int gymId = (int) session.getAttribute(gymIdKey);

        // ensure the gym has a wall
        if (!gymHasWall(gymId)) {
            // return unauthorized error message
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Gym has no wall"
            );
            return;
        }

        // get wall image file name from gym id
        String wallImageFileName;
        try {
            int wallId = getWallIdFromGymId(gymId);
            wallImageFileName = getWallImageFileNameFromWallId(wallId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }
        assert wallImageFileName != null;

        returnWallImageAsBitmap(response, wallImageFileName);
    }
}
