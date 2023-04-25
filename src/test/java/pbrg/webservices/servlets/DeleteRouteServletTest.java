package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.database.CredentialController.deleteUser;
import static pbrg.webservices.database.CredentialControllerTest
    .createTestUser;
import static pbrg.webservices.database.DatabaseTestMethods
    .mockConnectionThrowsException;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pbrg.webservices.database.DatabaseController;
import pbrg.webservices.utils.ServletUtils;

class DeleteRouteServletTest {

    /** The user id of the test user. */
    private int userId;

    /** The gym id of the test gym. */
    private int gymId;

    /** The wall id of the test wall. */
    private int wallId;

    /** The route id of the test route. */
    private int routeId;

    @BeforeAll
    public static void startResources() throws IllegalStateException {
        startTestDatabaseInThread();

        // use the test database
        DatabaseController.setDataSource(getTestDataSource());
    }

    @AfterAll
    static void closeResources() {
        closeTestDatabaseInThread();
    }

    @BeforeEach
    void createModels() {
        userId = createTestUser();
        gymId = createTestGym();
        wallId = createTestWall(gymId);
        routeId = createTestRoute(userId, wallId);
        assertTrue(routeExists(routeId));
    }

    @AfterEach
    void deleteModels() {
        // after: remove the database assets
        if (routeExists(routeId)) {
            assertTrue(deleteRoute(routeId));
        }
        assertTrue(deleteWall(wallId));
        assertTrue(deleteGym(gymId));
        assertTrue(deleteUser(userId));
    }

    @Test
    void testDoGetCallsDoPost() throws IOException {
        // given a request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        DeleteRouteServlet servlet = spy(new DeleteRouteServlet());

        // when calling do get
        servlet.doGet(request, response);

        // then do post is called
        verify(servlet).doPost(request, response);
    }

    @Test
    void testDoPostNoSession() throws IOException {
        // given a request without a session
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getSession(false)).thenReturn(null);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

        // then: the response is unauthorized
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

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(null);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

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

        // invalid route
        int invalidRouteId = -1;

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(invalidRouteId);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

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

        // given: a valid route
        assertTrue(routeExists(routeId));

        // given: route fails to delete (mock SQLException)
        DataSource originalDataSource = DatabaseController.getDataSource();
        DatabaseController.setDataSource(mockConnectionThrowsException());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

        verify(response).sendError(
            eq(HttpServletResponse.SC_EXPECTATION_FAILED),
            anyString()
        );

        // after: reset DataSource
        DatabaseController.setDataSource(originalDataSource);
    }

    @Test
    void testDoPostRouteImageFileDoesNotExist() throws IOException {
        // given: a valid route, with a thumbnail (to be deleted successfully)

        // mock the request and response
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // generate the route thumbnail, get
        assertTrue(createAndStoreRouteImage(routeId));

        // get route image
        String routeImage = getRouteImageFileName(routeId);
        assertNotNull(routeImage);

        // delete the route image locally
        File routeImageFile =
            new File(ServletUtils.getRouteImagePath(), routeImage);
        assertTrue(routeImageFile.delete());
        assertFalse(routeImageFile.exists());

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

        // then: route should still be removed
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertFalse(routeExists(routeId));
        assertFalse(routeImageFile.exists());
    }

    /** Test a partial route (one with no thumbnail generated). */
    @Test
    void testDoPostSuccessPartialRoute() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // given: a valid route (to be deleted successfully)
        assertTrue(routeExists(routeId));

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        // when deleting the route
        new DeleteRouteServlet().doPost(request, response);

        // then: ensure deleted
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertFalse(routeExists(routeId));
    }

    /** Test a full route (one with a thumbnail generated). */
    @Test
    void testDoPostSuccessFullRoute() throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        // given: a valid route, with a thumbnail (to be deleted successfully)
        assertTrue(routeExists(routeId));

        // generate the route thumbnail, get
        assertTrue(createAndStoreRouteImage(routeId));

        // get route image
        String routeImage = getRouteImageFileName(routeId);
        assertNotNull(routeImage);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("rid")).thenReturn(routeId);

        // when: deleting the route
        new DeleteRouteServlet().doPost(request, response);

        // then: ensure the thumbnail was deleted
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertFalse(routeExists(routeId));
        File routeImageFile =
            new File(ServletUtils.getRouteImagePath(), routeImage);
        assertFalse(routeImageFile.exists());
    }
}
