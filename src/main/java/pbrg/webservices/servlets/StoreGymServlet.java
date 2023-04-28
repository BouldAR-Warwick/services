package pbrg.webservices.servlets;

import static pbrg.webservices.database.AuthenticationController.userExists;
import static pbrg.webservices.database.GymController.getGym;
import static pbrg.webservices.database.GymController.getPrimaryGymOfUser;
import static pbrg.webservices.database.GymController.userHasPrimaryGym;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;
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

        // get user id from session
        HttpSession session = getSession(request);
        assert session != null;
        int userId = (int) session.getAttribute(userIdKey);

        // ensure the user id is valid
        if (!userExists(userId)) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid user id"
            );
            return;
        }

        // ensure the user has a primary gym
        if (!userHasPrimaryGym(userId)) {
            response.sendError(
                HttpServletResponse.SC_NOT_FOUND,
                "User has no primary gym"
            );
            return;
        }

        // get primary gym of user
        Integer gymId = getPrimaryGymOfUser(userId);
        assert gymId != null;
        Gym gym = getGym(gymId);
        assert gym != null;

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
