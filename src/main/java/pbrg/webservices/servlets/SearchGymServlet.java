package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import pbrg.webservices.models.GymList;
import pbrg.webservices.utils.Database;

@WebServlet(name = "SearchGymServlet", urlPatterns = "/SearchGym")
public class SearchGymServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final HttpServletRequest request, final HttpServletResponse response
    ) throws IOException {
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

        // get json object in the request body
        JSONObject jObj = new JSONObject(getBody(request));
        String queryWord = jObj.getString("queryword");

        // get all gyms matching query_word
        List<String> gyms = null;
        try {
            gyms = Database.getGymsByQueryWord(queryWord);
        } catch (Exception e) {
            response.getWriter().println(e.getMessage());
        }

        // error: unable to get gyms
        if (gyms == null) {
            response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
            return;
        }

        String[] gymArray = gyms.toArray(new String[0]);
        GymList gymList = new GymList(gymArray);
        String json = new Gson().toJson(gymList);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
