package pbrg.webservices.servlets;

import static pbrg.webservices.database.GymController.getGymByGymName;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
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
        String gymNameKey = "gymname";
        JSONObject bodyObject;
        try {
            bodyObject = new JSONObject(getBody(request));
            if (!bodyObject.has(gymNameKey)) {
                throw new JSONException("gym name is null");
            }
        } catch (JSONException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage()
            );
            return;
        }
        String gymName = bodyObject.getString(gymNameKey);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gym gym = getGymByGymName(gymName);
        if (gym == null) {
            // when no gyms are matched
            response.getWriter().write("{}");
            return;
        }

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);
    }
}
