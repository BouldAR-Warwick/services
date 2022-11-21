package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

@WebServlet(name = "GetWallImageServlet", urlPatterns = "/GetWallImage")
public class GetWallImageServlet extends MyHttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session==null) {
            // return unauthorized error message
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        PrintWriter out = response.getWriter();

        try
        {
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/grabourg");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT (WallImage) FROM WALLS WHERE GID = ?");
            pst.setInt(1, (int)session.getAttribute("gid"));
            ResultSet rs = pst.executeQuery();

            // get name of wall image
            String WallImage = "";
            if(rs.next()) {
                WallImage = rs.getString("WallImage");
            }
            pst.close();
            conn.close();

            // image directory
            FileInputStream fis = new FileInputStream("~/WallImages/" + WallImage) ;
            // get file size
            int size = fis.available();
            byte imageData[] = new byte[size] ;
            fis.read(imageData) ;
            fis.close();

            //set response to image type
            response.setContentType("image/jpeg");
            OutputStream os = response.getOutputStream() ;
            os.write(imageData);
            os.flush();
            os.close();
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }
    }
}

