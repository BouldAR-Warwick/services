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
import pbrg.webservices.utils.Database;
import pbrg.webservices.utils.Utils;

@WebServlet(name = "GetRouteImageServerlet", urlPatterns = "/GetRouteImage")
public class GetRouteImageServerlet extends MyHttpServlet {

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
    ) throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ensure session has route_id
        if (session.getAttribute("RID") == null) {
            // no route id in session
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        int routeId = (int) session.getAttribute("RID");

        // get the route image file name
        String imageFileName;
        try {
            imageFileName = Database.getRouteImageFileNamesByRouteId(
                routeId
            );
        } catch (SQLException exception) {
            response.getWriter().println(exception.getMessage());
            return;
        }

        // route does not exist
        if (imageFileName == null) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(imageFileName);
        String contentType = Utils.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] imageBuffer;
        try (FileInputStream fis = new FileInputStream(
            Utils.ROUTE_IMAGE_PATH + imageFileName)
        ) {
            int size = fis.available();
            imageBuffer = new byte[size];
            fis.read(imageBuffer);
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(imageBuffer);
            outputStream.flush();
        }
    }
}
