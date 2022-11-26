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
import pbrg.webservices.Singleton;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetRouteImageServerlet", urlPatterns = "/GetRouteImage")
public class GetRouteImage extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

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

        int route_id = (int) session.getAttribute("RID");

        // get the route image file name
        String image_file_name;
        try {
            image_file_name = Database.get_route_image_file_names_by_route_id(route_id);
        } catch (SQLException exception) {
            response.getWriter().println(exception.getMessage());
            return;
        }

        // route does not exist
        if (image_file_name == null) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(image_file_name);
        String contentType = Singleton.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] image_buffer;
        try (FileInputStream fis = new FileInputStream(Singleton.routeImagePath + image_file_name)) {
            int size = fis.available();
            image_buffer = new byte[size];
            int bytes_read = fis.read(image_buffer);

            if (bytes_read != size) {
                response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
                return;
            }
        }

        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(image_buffer);
            outputStream.flush();
        }
    }
}
