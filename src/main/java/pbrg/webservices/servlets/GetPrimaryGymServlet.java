package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.GymController.getGym;
import static pbrg.webservices.database.GymController.getPrimaryGymOfUser;
import static pbrg.webservices.database.GymController.userHasPrimaryGym;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import pbrg.webservices.models.Gym;

@WebServlet(name = "GetPrimaryGymServlet", urlPatterns = "/GetPrimaryGym")
public class GetPrimaryGymServlet extends MyHttpServlet {

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
        // validate request
        boolean requiresSession = true;
        String userIdKey = "uid";
        String[] sessionAttributes = {userIdKey};
        String[] bodyAttributes = {};
        if (!validateRequest(
            request, response, null, requiresSession,
            sessionAttributes, bodyAttributes
        )) {
            return;
        }

        // get the user id
        HttpSession session = getSession(request);
        assert session != null;
        int userId = (int) session.getAttribute("uid");

        // ensure the user id exists
        if (!userExists(userId)) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "User does not exist"
            );
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // check the user has a primary gym
        if (!userHasPrimaryGym(userId)) {
            response.getWriter().write("{}");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // get the primary gym of the user
        Integer gymId = getPrimaryGymOfUser(userId);
        assert gymId != null;
        Gym gym = getGym(gymId);

        session.setAttribute("gid", gym.getGid());
        String json = new Gson().toJson(gym);
        response.getWriter().write(json);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
