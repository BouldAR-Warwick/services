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
        // validate request
        boolean requiresSession = true;
        String[] sessionAttributes = {};
        String gymNameKey = "gymname";
        JSONObject body = getBodyAsJson(request);
        String[] bodyAttributes = {gymNameKey};
        if (!validateRequest(
            request, response, body, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // get session
        HttpSession session = getSession(request);
        assert session != null;

        // get gym name, gym from body
        assert body != null;
        String gymName = body.getString(gymNameKey);
        Gym gym = getGymByGymName(gymName);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (gym == null) {
            // when no gyms are matched
            response.getWriter().write("{}");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
