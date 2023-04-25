package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
import pbrg.webservices.database.GymController;
import pbrg.webservices.models.Gym;
import pbrg.webservices.utils.ServletUtils;

@WebServlet(name = "StoreGymServlet", urlPatterns = "/StoreGym")
public class StoreGymServlet extends MyHttpServlet {

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

        // ensure session has a user id
        String userIdKey = "uid";
        boolean sessionHasUserId = session.getAttribute(userIdKey) != null;
        if (!sessionHasUserId) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Session does not have a user id"
            );
            return;
        }

        // get user id from session
        int userId = (int) session.getAttribute("uid");

        // when no gyms are matched
        Gym gym = GymController.getPrimaryGymOfUser(userId);
        if (gym == null) {
            return;
        }

        // store gym ID in session cookie

        // create cookie and store logged-in user info in cookie
        // set expired time to 7 days
        Cookie gymIdCookie = new Cookie("gid", String.valueOf(gym.getGid()));
        int sevenDaysInSeconds =
            (int) Duration.ofDays(ServletUtils.SEVEN_DAYS).getSeconds();
        gymIdCookie.setMaxAge(sevenDaysInSeconds);

        // send cookie back to client for authentication next time
        response.addCookie(gymIdCookie);

        // report success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
