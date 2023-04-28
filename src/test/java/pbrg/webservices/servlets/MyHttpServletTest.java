package pbrg.webservices.servlets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.servlets.MyHttpServlet.getBody;
import static pbrg.webservices.servlets.MyHttpServlet.getBodyAsJson;
import static pbrg.webservices.servlets.MyHttpServlet.getSession;
import static pbrg.webservices.servlets.MyHttpServlet.validateBody;
import static pbrg.webservices.servlets.MyHttpServlet.validateRequest;
import static pbrg.webservices.servlets.MyHttpServlet.validateSession;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class MyHttpServletTest {

    /** The average Hueco grade. */
    private static final int AVERAGE_GRADE = 6;

    /** An example user id. */
    private static final int USER_ID = 123;

    static @NotNull HttpServletRequest mockRequestWithBody(
        final @NotNull String body
    ) throws IOException {
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
            ).thenAnswer(
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
        when(request.getContentLength()).thenReturn(myBinaryData.length);
        when(request.getReader())
            .thenReturn(new BufferedReader(new InputStreamReader(bodyStream)));

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
        assertNotNull(body);

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
        assertNotNull(body);

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

    @Test
    void testGetBodyException() throws IOException {
        // given: an IOException is thrown when getInputStream is called
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenThrow(new IOException());

        // when: getBody is called, then: null is returned
        assertNull(getBody(request));
    }

    @Test
    void testGetBodyAsJsonNullBody() throws IOException {
        // given: an IOException is thrown when getInputStream is called
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getReader()).thenThrow(new IOException());

        // when: getBody is called, then: null is returned
        assertNull(getBodyAsJson(request));
    }

    @Test
    void testGetBodyJsonDoubleQuoteEncoded() throws IOException {
        // given: a request with a body
        String bodyString = "{\"queryword\":\"test\"}";
        HttpServletRequest request = mockRequestWithBody(bodyString);

        // when: getBodyAsJson is called
        JSONObject body = getBodyAsJson(request);

        // then: the body is parsed as a JSONObject
        assertNotNull(body);
        assertTrue(body.has("queryword"));
        assertEquals("test", body.getString("queryword"));
    }

    @Test
    void testGetInputStreamTwice() throws IOException {
        // given: a request with a body
        String body = "{\"difficulty\": 6}";
        HttpServletRequest request = mockRequestWithBody(body);

        // when: getInputStream is called twice
        InputStream inputStream = request.getInputStream();
        InputStream inputStream2 = request.getInputStream();

        // then: the body is parsed only once
        assertEquals(inputStream, inputStream2);
    }

    @Test
    void testGetAvailableTwice() throws IOException {
        // given: a request with a body
        String body = "{\"difficulty\": 6}";
        HttpServletRequest request = mockRequestWithBody(body);

        // when: request.getInputStream().available() is called twice
        int available1 = request.getInputStream().available();
        int available2 = request.getInputStream().available();

        // then: the body is parsed only once
        assertEquals(available1, available2);
    }

    @Nested
    class TestValidateSession {

        @Test
        void validTwoAttributes() throws IOException {
            // given a valid session with two attributes
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
            when(request.getSession(anyBoolean())).thenReturn(session);
            when(session.getAttribute("attribute1")).thenReturn("value1");
            when(session.getAttribute("attribute2")).thenReturn("value2");

            // when validateSession is called with the two attributes
            // then session is valid
            String[] sessionAttributes = {"attribute1", "attribute2"};
            assertTrue(validateSession(request, response, sessionAttributes));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        void invalidNullSession() throws IOException {
            // given: a null session
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(request.getSession(anyBoolean())).thenReturn(null);

            // when: validateSession is called, then: false is returned
            String[] sessionAttributes = {"attribute1", "attribute2"};
            assertFalse(validateSession(request, response, sessionAttributes));

            // and: an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_UNAUTHORIZED), anyString()
            );
        }

        @Test
        void invalidMissingAttributes() throws IOException {
            // given: a session without the required attributes
            HttpSession session = mock(HttpSession.class);
            when(session.getAttribute("attribute1"))
                .thenReturn("value1");

            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(request.getSession(anyBoolean())).thenReturn(session);

            // when: validateSession is called, then: false is returned
            String[] sessionAttributes = {"attribute1", "attribute2"};
            assertFalse(validateSession(request, response, sessionAttributes));

            // and: an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_UNAUTHORIZED), anyString()
            );
        }

        @Test
        void validNoAttributes() throws IOException {
            // given a valid session with no attributes
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            HttpSession session = mock(HttpSession.class);
            when(request.getSession(anyBoolean())).thenReturn(session);

            // when validateSession is called with no attributes
            // then session is valid
            String[] sessionAttributes = {};
            assertTrue(validateSession(request, response, sessionAttributes));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }
    }

    @Nested
    class TestValidateBody {
        @Test
        void noParameters() throws IOException {
            // given a request body with no parameters
            HttpServletResponse response = mock(HttpServletResponse.class);
            String[] emptyParameters = {};

            // ensure is valid
            assertTrue(validateBody(response, null, emptyParameters));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        void invalidBody() throws IOException {
            // given a request body with no parameters
            String bodyString = "";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);
            JSONObject body = getBodyAsJson(request);
            String[] parameters = {"parameter1"};

            // ensure is invalid
            assertFalse(validateBody(response, body, parameters));

            // and an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_BAD_REQUEST), anyString()
            );
        }

        @Test
        void invalidBodyNoAttributes() throws IOException {
            // given a request body with no parameters
            String bodyString = "{}";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);
            JSONObject body = getBodyAsJson(request);
            String[] parameters = {"parameter1"};

            // ensure is invalid
            assertFalse(validateBody(response, body, parameters));

            // and an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_BAD_REQUEST), anyString()
            );
        }

        @Test
        void validOneAttribute() throws IOException {
            // given a request body with no parameters
            String bodyString = "{parameter1: 'value1'}";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);
            JSONObject body = getBodyAsJson(request);
            String[] parameters = {"parameter1"};

            // ensure is valid
            assertTrue(validateBody(response, body, parameters));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        void validOneAttributeQuoteEncoded() throws IOException {
            // given a request body with no parameters
            String bodyString = "{\"parameter1\": 'value1'}";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);
            JSONObject body = getBodyAsJson(request);
            String[] parameters = {"parameter1"};

            // ensure is valid
            assertTrue(validateBody(response, body, parameters));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }
    }

    @Nested
    class TestValidateRequest {
        @Test
        void validSessionAndBody() throws IOException {
            // given a valid session and body
            String bodyString = "{parameter1: 'value1'}";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);

            HttpSession session = mock(HttpSession.class);
            when(request.getSession(anyBoolean())).thenReturn(session);
            when(session.getAttribute("attribute1")).thenReturn("value1");
            when(session.getAttribute("attribute2")).thenReturn("value2");

            // when validateRequest is called with the two attributes
            // then session is valid
            boolean hasSession = true;
            String[] sessionAttributes = {"attribute1", "attribute2"};
            JSONObject body = getBodyAsJson(request);
            String[] bodyParameters = {"parameter1"};
            assertTrue(validateRequest(
                request, response, body, hasSession,
                sessionAttributes, bodyParameters
            ));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }

        @Test
        void invalidNullSession() throws IOException {
            // given an invalid session
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            when(request.getSession(anyBoolean())).thenReturn(null);

            // when validateRequest is called with the two attributes
            // then session is invalid
            boolean hasSession = true;
            String[] sessionAttributes = {};
            String[] bodyParameters = {};
            assertFalse(validateRequest(
                request, response, null, hasSession,
                sessionAttributes, bodyParameters
            ));

            // and an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_UNAUTHORIZED), anyString()
            );
        }

        @Test
        void invalidBody() throws IOException {
            // given a valid session and invalid body
            String bodyString = "";
            HttpServletRequest request = mockRequestWithBody(bodyString);
            HttpServletResponse response = mock(HttpServletResponse.class);

            // when validateRequest is called with the two attributes
            // then session is invalid
            boolean hasSession = false;
            String[] sessionAttributes = {};
            JSONObject body = getBodyAsJson(request);
            String[] bodyParameters = {"parameter1"};
            assertFalse(validateRequest(
                request, response, body, hasSession,
                sessionAttributes, bodyParameters
            ));

            // and an error is sent
            verify(response).sendError(
                eq(HttpServletResponse.SC_BAD_REQUEST), anyString()
            );
        }

        @Test
        void validNeitherRequired() throws IOException {
            // given neither are required
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            boolean hasSession = false;
            String[] sessionAttributes = {};
            JSONObject body = getBodyAsJson(request);
            String[] bodyParameters = {};

            // when testing for validity
            // then request is valid
            assertTrue(validateRequest(
                request, response, body, hasSession,
                sessionAttributes, bodyParameters
            ));

            // and no error is sent
            verify(response, never()).sendError(anyInt(), anyString());
        }
    }
}
