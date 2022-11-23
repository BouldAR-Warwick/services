package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import pbrg.webservices.Singleton;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;


@WebServlet(name = "GetRoutesServerlet", urlPatterns = "/getRoutes")
public class GetRoutesServerlet extends MyHttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
        assert gymID != null;

        PrintWriter out = response.getWriter();

        Connection conn = Singleton.getDbConnection();

        try (PreparedStatement pst = conn.prepareStatement(
            "SELECT ROUTES.RID " + 
            "FROM ROUTES " + 
            "INNER JOIN WALLS ON ROUTES.WID = WALLS.WID " +
            "INNER JOIN GYMS ON WALLS.GID = GYMS.GID " +
            "WHERE GYMS.GID = ?"
        )) {
            pst.setString(1, "%"+gymID+"%");

            ResultSet rs = pst.executeQuery();

            ArrayList<String> routeIDs = new ArrayList<String>();
            while(rs.next()) {
                routeIDs.add(rs.getString("RID"));
            }

            String[] arrayOfRouteIDs = routeIDs.toArray(new String[routeIDs.size()]);

            String json = new Gson().toJson(arrayOfRouteIDs);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            pst.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Singleton.closeDbConnection();
    }
}