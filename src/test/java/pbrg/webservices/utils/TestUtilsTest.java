package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class TestUtilsTest {

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<TestUtils> constructor;
        try {
            constructor = TestUtils.class.getDeclaredConstructor();
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

    @Test
    void python3Installed() {
        assertTrue(TestUtils.python3Installed(), "ensure python3 is installed");
    }

    @Test
    void testPython3InstalledIOException() throws Exception {
        // given: starting process throws IOException
        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        when(mockProcessBuilder.start()).thenThrow(new IOException());

        // when: runCommand is called
        // then: return false
        assertFalse(TestUtils.runCommand(mockProcessBuilder));
    }

    @Test
    void testPython3InstalledInterruptedException() throws Exception {
        // given: waiting for process throws IOException
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor()).thenThrow(new InterruptedException());

        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);

        // when: runCommand is called
        // then: return false
        assertFalse(TestUtils.runCommand(mockProcessBuilder));
    }

    @Test
    void testPython3InstalledNegative() throws Exception {
        // given: process returns non-zero exit code
        Process mockProcess = mock(Process.class);
        when(mockProcess.waitFor()).thenReturn(1);

        ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);

        // when: runCommand is called
        // then: return false
        assertFalse(TestUtils.runCommand(mockProcessBuilder));
    }
}
