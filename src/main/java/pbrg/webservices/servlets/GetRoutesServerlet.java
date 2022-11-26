package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pbrg.webservices.Singleton;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.*;


@WebServlet(name = "GetRoutesServerlet", urlPatterns = "/getRoutes")
public class GetRoutesServerlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session==null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get json object in the request body
        JSONObject jObj = new JSONObject(getBody(request));

        if (!jObj.has("gymID")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String gymID = jObj.getString("gymID");

        try {
            Connection conn = Singleton.getDbConnection();
            PreparedStatement pst = conn.prepareStatement(
            "SELECT routes.RID " +
                "FROM routes " +
                "INNER JOIN walls ON routes.WID = walls.WID " +
                "INNER JOIN gyms ON walls.GID = gyms.GID " +
                "WHERE gyms.GID = ?"
            );
            pst.setString(1, "%"+gymID+"%");

            ResultSet rs = pst.executeQuery();

            List<String> routeIDs = new ArrayList<>();
            while(rs.next()) {
                routeIDs.add(rs.getString("RID"));
            }

            String[] arrayOfRouteIDs = routeIDs.toArray(new String[0]);

            String json = new Gson().toJson(arrayOfRouteIDs);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            pst.close();
            Singleton.closeDbConnection();

        } catch (SQLException e) {
            response.getWriter().println(e.getMessage());
        }
    }
}