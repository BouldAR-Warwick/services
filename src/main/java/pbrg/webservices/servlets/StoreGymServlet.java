package pbrg.webservices.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
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
        HttpSession session = getSession(request);

        // return unauthorized error message if session is not exist
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("uid");

        Gym gym = null;
        try {
            gym = GymController.getGymByUserId(userId);
        } catch (SQLException exception) {
            response.getWriter().println(exception.getMessage());
        }

        // when no gyms are matched
        if (gym == null) {
            return;
        }

        // store gym ID in session cookie

        // create cookie and store logged-in user info in cookie
        Cookie gymIdCookie = new Cookie("gid", String.valueOf(gym.getGid()));

        // set expired time to 7 days
        int sevenDaysInSeconds =
            (int) Duration.ofDays(ServletUtils.SEVEN_DAYS).getSeconds();
        gymIdCookie.setMaxAge(sevenDaysInSeconds);

        // send cookie back to client for authentication next time
        response.addCookie(gymIdCookie);
    }
}
