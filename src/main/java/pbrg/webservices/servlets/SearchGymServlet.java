package pbrg.webservices.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.google.gson.Gson;
import org.json.JSONObject;
import java.util.*;

import pbrg.webservices.models.GymList;

@WebServlet(name = "SearchGymServlet", urlPatterns = "/SearchGym")
public class SearchGymServlet extends MyHttpServlet {
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
        String queryword = jObj.getString("queryword");

        PrintWriter out = response.getWriter();

        try
        {
            // get a database connection from connection pool
            Context ctx = new InitialContext();
            DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/grabourg");
            Connection conn = ds.getConnection();
            PreparedStatement pst = conn.prepareStatement("SELECT Gymname FROM gyms WHERE GymLocation LIKE ? OR Gymname LIKE ?");
            pst.setString(1, "%"+queryword+"%");
            pst.setString(2, "%"+queryword+"%");
            ResultSet rs = pst.executeQuery();

            List<String> gyms = new ArrayList<>();
            while(rs.next()) {
                gyms.add(rs.getString("Gymname"));
            }
            String[] gymArray = gyms.toArray(new String[0]);
            GymList gymList = new GymList(gymArray);
            String json = new Gson().toJson(gymList);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            pst.close();
            conn.close();
        }
        catch(Exception e)
        {
            out.println(e.getMessage());
        }
    }
}
