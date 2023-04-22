package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pbrg.webservices.utils.ServletUtils.getContentType;
import static pbrg.webservices.utils.ServletUtils.returnImageAsBitmap;
import static pbrg.webservices.utils.ServletUtils.returnRouteImageAsBitmap;
import static pbrg.webservices.utils.ServletUtils.returnWallImageAsBitmap;
import static pbrg.webservices.utils.ServletUtils.sessionHasAttributes;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServletUtilsTest {

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<ServletUtils> constructor;
        try {
            constructor = ServletUtils.class.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            fail("DatabaseUtils should have a private constructor");
            throw new RuntimeException(e);
        }

        // ensure calling constructor throws an IllegalStateException exception
        constructor.setAccessible(true);
        try {
            constructor.newInstance();
            fail("Expected IllegalStateException to be thrown");
        } catch (
            InvocationTargetException | InstantiationException
            | IllegalAccessException e
        ) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    @BeforeAll
    static void setUp() {
        if (!TestUtils.python3Installed()) {
            throw new RuntimeException("Python 3 not installed.");
        }
    }

    @Test
    void getContentTypeTest() {
        assertEquals("image/jpeg", getContentType("jpg"));

        // ensure non keys are mapped to null
        String[] nonKeys = {null, "", "not-in-map"};
        for (String nonKey : nonKeys) {
            assertNull(getContentType(nonKey));
        }
    }

    @Test
    void testSessionHasAttributes() {
        // Given
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("attr1")).thenReturn("value1");
        when(session.getAttribute("attr2")).thenReturn("value2");

        // When & Then
        assertTrue(
            sessionHasAttributes(session, new String[]{"attr1", "attr2"})
        );
        assertFalse(
            sessionHasAttributes(session, new String[]{"attr1", "attr3"})
        );
    }

    @Test
    void testReturnImageAsBitmap() throws IOException {
        // Given
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        String filePath = ServletUtils.getWallImagePath() + "MoonBoard2016.jpg";

        // When
        returnImageAsBitmap(response, filePath);

        // Then
        verify(response).setContentType("image/jpeg");
        verify(outputStream).write(any(byte[].class));
    }

    @Test
    void testReturnImageAsBitmapWithInvalidFile() throws IOException {
        // Given
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);

        String filePath = "invalid.png";

        // When
        assertThrows(
            // then: expect a FileNotFoundException
            FileNotFoundException.class,

            // when: returnImageAsBitmap is called
            () -> returnImageAsBitmap(response, filePath)
        );
    }

    @Test
    void testReturnRouteImageAsBitmap() throws IOException {
        // given a route image
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        String fileName = "r1.jpg";

        // when
        returnRouteImageAsBitmap(response, fileName);

        // then
        verify(response).setContentType("image/jpeg");
        verify(outputStream).write(any(byte[].class));
    }

    @Test
    void testReturnWallImageAsBitmap() throws IOException {
        // given a route image
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(outputStream);
        String fileName = "MoonBoard2016.jpg";

        // when
        returnWallImageAsBitmap(response, fileName);

        // then
        verify(response).setContentType("image/jpeg");
        verify(outputStream).write(any(byte[].class));
    }

    @Test
    void returnImageAsBitmapUnsupportedFile() {
        // given an unsupported file
        String fileName = "unsupported.txt";
        assertNull(getContentType(fileName));

        assertThrows(
            // then: expect a IOException
            IOException.class,

            // when: returnImageAsBitmap is called
            () -> returnImageAsBitmap(mock(HttpServletResponse.class), fileName)
        );
    }

    @Test
    void getBytesFromFileInputStreamNonMatchingSizes() throws IOException {
        FileInputStream mockedFis = mock(FileInputStream.class);
        when(mockedFis.available()).thenReturn(0);
        when(mockedFis.read(any(byte[].class))).thenReturn(1);

        assertThrows(
            // then: expect a IOException
            IOException.class,

            // when: getBytesFromFileInputStream is called
            () -> ServletUtils.getBytesFromFileInputStream(mockedFis)
        );
    }

}
