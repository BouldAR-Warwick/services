package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pbrg.webservices.database.GymController;
import pbrg.webservices.models.GymList;

@WebServlet(name = "SearchGymServlet", urlPatterns = "/SearchGym")
public class SearchGymServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return;
        }

        // parse the request body
        JSONObject body = getBodyAsJson(request);
        if (body == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not a valid JSON object"
            );
            return;
        }

        // ensure the request body contains the query word
        if (!body.has("queryword")) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body does not contain the query word"
            );
            return;
        }

        // get the query word
        String queryWord = body.getString("queryword");

        // get all gyms matching the query word
        List<String> gyms = GymController.getGymsByQueryWord(queryWord);

        // convert the list of gyms to a json string
        String[] gymArray = gyms.toArray(new String[0]);
        GymList gymList = new GymList(gymArray);
        String json = new Gson().toJson(gymList);

        // return the list of gyms as a json string
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
