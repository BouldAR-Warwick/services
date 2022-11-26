package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.io.FilenameUtils;
import pbrg.webservices.Singleton;
import pbrg.webservices.utils.Database;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        int gym_id = (int) session.getAttribute("gid");

        // get wall image file name from gym id
        String image_file_name = null;
        try {
            Connection connection = Singleton.getDbConnection();
            image_file_name = Database.get_wall_image_file_name_from_gym_id(gym_id, connection);
            Singleton.closeDbConnection();
        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
        }

        // wall query failed or no wall against gym
        if (image_file_name == null) {
            // case gym has no wall! - TODO
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(image_file_name);
        String contentType = Singleton.getContentType(ext);
        response.setContentType(contentType);

        // read-in image file
        byte[] image_buffer;
        try (FileInputStream fis = new FileInputStream(Singleton.wallImagePath + image_file_name)) {
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
