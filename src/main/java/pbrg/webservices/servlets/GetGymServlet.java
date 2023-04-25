package pbrg.webservices.servlets;

import static pbrg.webservices.database.GymController.getGymByGymName;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
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
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // get json object in the request body
        String gymNameKey = "gymname";
        JSONObject arguments = getBodyAsJson(request);
        if (arguments == null) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is not a valid JSON object"
            );
            return;
        }

        // ensure request has gym name
        if (!arguments.has(gymNameKey)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Request body is missing " + gymNameKey
            );
            return;
        }

        // get gym name, gym
        String gymName = arguments.getString(gymNameKey);
        Gym gym = getGymByGymName(gymName);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (gym == null) {
            // when no gyms are matched
            response.getWriter().write("{}");
            return;
        }

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
