package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.GymController.getPrimaryGymOfUser;

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
        // ensure session exists
        HttpSession session = getSession(request);
        if (session == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not exist"
            );
            return;
        }

        // ensure the session has a user id
        String userIdKey = "uid";
        boolean sessionHasUserId = session.getAttribute(userIdKey) != null;
        if (!sessionHasUserId) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not have a user id"
            );
            return;
        }

        // ensure the user id exists
        int userId = (int) session.getAttribute("uid");
        if (!userExists(userId)) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "User does not exist"
            );
            return;
        }

        Gym gym = getPrimaryGymOfUser(userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // when no gyms are matched
        if (gym == null) {
            response.getWriter().write("{}");
        } else {
            session.setAttribute("gid", gym.getGid());
            String json = new Gson().toJson(gym);
            response.getWriter().write(json);
        }

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
