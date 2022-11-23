package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pbrg.webservices.Singleton;

import org.json.JSONObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
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

        PrintWriter out = response.getWriter();

        Connection conn = Singleton.getDbConnection();

        try (PreparedStatement pst = conn.prepareStatement(
            "SELECT (walls.image_file_name) " + 
            "FROM walls " + 
            "WHERE GID = ?"
        )) {
            pst.setInt(1, gymID);
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            String image_file_name = "";
            if (rs.next()) {
                image_file_name = rs.getString("image_file_name");
            }
            pst.close();
            conn.close();

            // get the file extension, lookup content type
            String ext = FilenameUtils.getExtension(image_file_name);
            String contentType = Singleton.getContentType(ext);

            // image directory
            FileInputStream fis = new FileInputStream(Singleton.wallImagePath + image_file_name);

            // get file size
            int size = fis.available();
            byte imageData[] = new byte[size];
            fis.read(imageData);
            fis.close();

            // set response to image type
            response.setContentType(contentType);
            OutputStream os = response.getOutputStream();
            os.write(imageData);
            os.flush();
            os.close();
        } catch (IllegalArgumentException | SQLException e) {
            out.println(e.getMessage());
        }
    }
}
