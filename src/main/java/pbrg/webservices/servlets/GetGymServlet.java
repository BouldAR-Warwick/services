package pbrg.webservices.servlets;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import pbrg.webservices.database.GymController;
import pbrg.webservices.models.Gym;

@WebServlet(name = "GetGymServlet", urlPatterns = "/GetGym")
public class GetGymServlet extends MyHttpServlet {

    @Override
    protected final void doGet(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    )
        throws IOException {
        doPost(request, response);
    }

    @Override
    protected final void doPost(
        final @NotNull HttpServletRequest request,
        final @NotNull HttpServletResponse response
    ) throws IOException {

        // get session or return unauthorized error message
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get json object in the request body
        JSONObject jObj = new JSONObject(getBody(request));
        String gymName = jObj.getString("gymname");

        Gym gym = null;
        try {
            gym = GymController.getGymByGymName(gymName);
        } catch (Exception e) {
            response.getWriter().println(e.getMessage());
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // when no gyms are matched
        if (gym == null) {
            response.getWriter().write("{}");
            return;
        }

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);
    }
}
