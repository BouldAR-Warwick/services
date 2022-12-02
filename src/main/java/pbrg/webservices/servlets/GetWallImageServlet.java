package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import org.apache.commons.io.FilenameUtils;
import pbrg.webservices.utils.DatabaseController;
import pbrg.webservices.utils.Utils;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final HttpServletRequest request, final HttpServletResponse response
    )
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final HttpServletRequest request, final HttpServletResponse response
    )
        throws IOException {
        if (request == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // get session, error if request is unauthorized
        HttpSession session = getSession(request);
        if (session == null) {
            // return unauthorized error message
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (session.getAttribute("gid") == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int gymId = (int) session.getAttribute("gid");

        // get wall image file name from gym id
        String imageFileName;
        try {
            imageFileName = DatabaseController
                .getWallImageFileNameFromGymId(gymId);
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
            return;
        }

        // wall query failed or no wall against gym
        if (imageFileName == null) {
            // case gym has no wall! - TODO
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(imageFileName);
        String contentType = Utils.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer;
        try (
            FileInputStream fis = new FileInputStream(
                Utils.WALL_IMAGE_PATH + imageFileName
            )
        ) {
            int size = fis.available();
            imageBuffer = new byte[size];
            int bytesRead = fis.read(imageBuffer);

            if (size != bytesRead) {
                response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
                return;
            }
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(imageBuffer);
            outputStream.flush();
        }
    }
}
