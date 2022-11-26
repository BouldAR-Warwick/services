package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pbrg.webservices.Singleton;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/* For parsing file extensions */
import org.apache.commons.io.FilenameUtils;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
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

        int gymID = (int) session.getAttribute("gid");

        byte[] imageData;
        String image_file_name = "";

        try {
            Connection connection = Singleton.getDbConnection();
            PreparedStatement pst = connection.prepareStatement(
            "SELECT (walls.image_file_name) " +
                "FROM walls " +
                "WHERE GID = ?"
            );
            pst.setInt(1, gymID);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            if (rs.next()) {
                image_file_name = rs.getString("image_file_name");
            }

            Singleton.closeDbConnection();
        } catch (IllegalArgumentException | SQLException e) {
            PrintWriter out = response.getWriter();
            out.println(e.getMessage());
        }

        if (image_file_name == null) {
            // case gym has no wall! - TODO
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        // get the file extension, lookup & set content type
        String ext = FilenameUtils.getExtension(image_file_name);
        String contentType = Singleton.getContentType(ext);
        response.setContentType(contentType);

        // image directory
        FileInputStream fis = new FileInputStream(Singleton.wallImagePath + image_file_name);

        // get file size
        int size = fis.available();
        imageData = new byte[size];
        fis.read(imageData);
        fis.close();

        OutputStream outputStream = response.getOutputStream();
        outputStream.write(imageData);
        outputStream.flush();
        outputStream.close();
    }
}
