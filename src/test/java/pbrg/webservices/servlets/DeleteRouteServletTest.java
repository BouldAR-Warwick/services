package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.CredentialController.deleteUser;
import static pbrg.webservices.database.CredentialControllerTest.createTestUser;
import static pbrg.webservices.database.DatabaseTestMethods.mockConnectionThrowsException;
import static pbrg.webservices.database.GymController.deleteGym;
import static pbrg.webservices.database.GymControllerTest.createTestGym;
import static pbrg.webservices.database.RouteController.deleteRoute;
import static pbrg.webservices.database.RouteController.routeExists;
import static pbrg.webservices.database.RouteControllerTest.createTestRoute;
import static pbrg.webservices.database.TestDatabase.closeTestDatabaseInThread;
import static pbrg.webservices.database.TestDatabase.getTestDataSource;
import static pbrg.webservices.database.TestDatabase.startTestDatabaseInThread;
import static pbrg.webservices.database.WallController.deleteWall;
import static pbrg.webservices.database.WallControllerTest.createTestWall;
import static pbrg.webservices.utils.RouteUtils.createAndStoreRouteImage;
import static pbrg.webservices.utils.RouteUtils.getRouteImageFileName;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.ServletUtils;

class DeleteRouteServletTest {

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
    void testDoPostNoSession() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        when(request.getSession(false)).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED),
            anyString()
        );
    }

    @Test
    void testDoPostNoRouteIdInSession() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(null);

        servlet.doPost(request, response);

        verify(response).sendError(
            eq(HttpServletResponse.SC_BAD_REQUEST),
            anyString()
        );
    }

    @Test
    void testDoPostRouteDoesNotExist() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        // invalid route
        int routeId = -1;

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        servlet.doPost(request, response);

        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );
    }

    @Test
    void testDoPostFailedToDeleteRoute() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        // given: a valid route
        int userId = createTestUser();
        int gymId = createTestGym();
        int wallId = createTestWall(gymId);
        int routeId = createTestRoute(userId, wallId);
        assertTrue(routeExists(routeId));

        // given: route fails to delete (mock SQLException)
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        servlet.doPost(request, response);

        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after: reset DataSource
        DatabaseController.setDataSource(originalDataSource);

        // after: remove the database assets
        assertTrue(deleteRoute(routeId));
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }

    /** Test a partial route (one with no thumbnail generated). */
    @Test
    void testDoPostSuccessPartialRoute() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        // given: a valid route (to be deleted successfully)
        int userId = createTestUser();
        int gymId = createTestGym();
        int wallId = createTestWall(gymId);
        int routeId = createTestRoute(userId, wallId);
        assertTrue(routeExists(routeId));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        servlet.doPost(request, response);

        // then: ensure success
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertFalse(routeExists(routeId));

        // after: remove the database assets
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }

    /** Test a full route (one with a thumbnail generated). */
    @Test
    void testDoPostSuccessFullRoute() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        DeleteRouteServlet servlet = new DeleteRouteServlet();

        // given: a valid route, with a thumbnail (to be deleted successfully)
        int userId = createTestUser();
        int gymId = createTestGym();
        int wallId = createTestWall(gymId);
        int routeId = createTestRoute(userId, wallId);
        assertTrue(routeExists(routeId));

        // generate the route thumbnail, get
        assertTrue(createAndStoreRouteImage(routeId));

        // get route image
        String routeImage = getRouteImageFileName(routeId);
        assertNotNull(routeImage);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        servlet.doPost(request, response);

        // then: ensure success
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertFalse(routeExists(routeId));

        // then: ensure the thumbnail was deleted
        File routeImageFile =
            new File(ServletUtils.getRouteImagePath(), routeImage);
        assertFalse(routeImageFile.exists());

        // after: remove the database assets
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }
}
