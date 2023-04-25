package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.servlets.MyHttpServlet.getBody;
import static pbrg.webservices.servlets.MyHttpServlet.getSession;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class MyHttpServletTest {

    /** The average Hueco grade. */
    private static final int AVERAGE_GRADE = 6;

    /** An example user id. */
    private static final int USER_ID = 123;

    static HttpServletRequest mockRequestWithBody(final String body)
        throws IOException {
        // mock the ServletInputStream object
        byte[] myBinaryData = body.getBytes();
        ByteArrayInputStream byteArrayInputStream =
            new ByteArrayInputStream(myBinaryData);
        ServletInputStream bodyStream = mock(ServletInputStream.class);
        when(bodyStream.read(any(byte[].class), anyInt(), anyInt()))
            .thenAnswer(
                (Answer<Integer>) invocationOnMock -> {
                    Object[] args = invocationOnMock.getArguments();
                    byte[] output = (byte[]) args[0];
                    int offset = (int) args[1];
                    int length = (int) args[2];
                    return byteArrayInputStream.read(output, offset, length);
                }
            );
        when(bodyStream.available()).thenReturn(myBinaryData.length);
        assertNotNull(bodyStream);

        // Create a mock HttpServletRequest object
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(bodyStream);

        return request;
    }

    @Test
    void getBodyOfValidJSONRequest() throws IOException {
        // difficulty key, value pair
        String key = "difficulty";
        int value = AVERAGE_GRADE;

        // JSON string containing the field "difficulty" with the value 6
        String bodyJsonString = "{\"" + key + "\": " + value + "}";

        // mock the request
        HttpServletRequest request = mockRequestWithBody(bodyJsonString);

        // Call the getBody method with the mock request
        String body = getBody(request);

        // Verify that the body contains the expected JSON string
        assertEquals(bodyJsonString, body);

        JSONObject bodyAsJson = new JSONObject(body);
        assertTrue(bodyAsJson.has(key));
        assertEquals(value, bodyAsJson.getInt(key));
    }

    @Test
    void getBodyOfJSONRequestKeyNotWrapped() throws IOException {
        // difficulty key, value pair
        String key = "difficulty";
        int value = AVERAGE_GRADE;

        // JSON string containing the field "difficulty" with the value 6
        String bodyJsonString = "{" + key + ": " + value + "}";

        // mock the request
        HttpServletRequest request = mockRequestWithBody(bodyJsonString);

        // Call the getBody method with the mock request
        String body = getBody(request);

        // Verify that the body contains the expected JSON string
        assertEquals(bodyJsonString, body);

        JSONObject bodyAsJson = new JSONObject(body);
        assertTrue(bodyAsJson.has(key));
        assertEquals(value, bodyAsJson.getInt(key));
    }

    @Test
    void testGetSessionWhenSessionExists() {
        // arrange
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(session);

        // act
        HttpSession result = getSession(request);

        // assert
        assertEquals(session, result);
        verify(request).getSession(false);
    }

    @Test
    void testGetSessionWhenSessionDoesNotExistAndCookieHasUid() {
        // arrange
        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies())
            .thenReturn(new Cookie[] {
                new Cookie("uid", String.valueOf(USER_ID))
            });
        when(request.getSession(true)).thenReturn(session);

        // act
        HttpSession result = getSession(request);

        // assert
        assertEquals(session, result);
        verify(request).getSession(false);
        verify(request).getCookies();
        verify(request).getSession(true);
        verify(session).setAttribute("uid", USER_ID);
    }

    @Test
    void testGetSessionWhenSessionDoesNotExistAndCookieHasNoUid() {
        // arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies())
            .thenReturn(new Cookie[] {new Cookie("name", "Alice")});

        // act
        HttpSession result = getSession(request);

        // assert
        assertNull(result);
        verify(request).getSession(false);
        verify(request).getCookies();
    }

    @Test
    void testGetSessionWhenSessionDoesNotExistAndCookieIsNull() {
        // arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        // act
        HttpSession result = getSession(request);

        // assert
        assertNull(result);
        verify(request).getSession(false);
        verify(request).getCookies();
    }

    @Test
    void hitPost() {
        assertDoesNotThrow(() ->
            new MyHttpServlet().doPost(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class)
            )
        );
    }

    @Test
    void hitGet() {
        assertDoesNotThrow(() ->
            new MyHttpServlet().doGet(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class)
            )
        );
    }
}
