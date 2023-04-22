package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.CredentialController.deleteUser;
import static pbrg.webservices.database.CredentialController.userExists;
import static pbrg.webservices.database.CredentialControllerTest
    .createTestUser;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymController.gymExists;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.RouteController.deleteRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.addWall;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.servlets.MyHttpServletTest.mockRequestWithBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;

class GenerateRouteServletTest {

    /** The average Hueco grade. */
    private static final int AVERAGE_GRADE = 6;

    @BeforeAll
    static void startResources() throws IllegalStateException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @Test
    void validRunThrough() throws SQLException, IOException {
        // given: a user, a gym with a wall, a grade
        int uid = createTestUser();
        int gid = createTestGym();

        Integer wallId = addWall(
            gid, "MoonBoard", "MoonBoard2016.jpg"
        );
        assertNotNull(wallId);

        // mock session
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("uid")).thenReturn(uid);
        when(session.getAttribute("gid")).thenReturn(gid);

        // mock request
        String body = "{\"difficulty\": " + AVERAGE_GRADE + "}";
        HttpServletRequest request = mockRequestWithBody(body);
        when(request.getSession(anyBoolean())).thenReturn(session);

        // mock response
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        // when: the servlet is called
        new GenerateRouteServlet().doPost(request, response);

        // after: remove models

        assertTrue(deleteRoute(uid, wallId));
        assertTrue(deleteWall(wallId));

        assertTrue(deleteGym(gid));
        assertFalse(gymExists(gid));

        assertTrue(deleteUser(uid));
        assertFalse(userExists(uid));

        // then: verify the response contains the route Id
        String responseString = sw.getBuffer().toString().trim();
        System.out.println(responseString);
    }
}
