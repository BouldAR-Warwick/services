package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.utils.ProcessUtils.collectOutput;
import static pbrg.webservices.utils.ProcessUtils.collectOutputAsList;
import static pbrg.webservices.utils.ProcessUtils.getExitCode;
import static pbrg.webservices.utils.ProcessUtils.readLines;
import static pbrg.webservices.utils.ProcessUtils.runProcessBuilder;
import static pbrg.webservices.utils.ProcessUtils.runProcessEnsureSuccess;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessUtilsTest {

    @Test
    void testPrivateConstructor() {
        // get constructor
        Constructor<ProcessUtils> constructor;
        try {
            constructor = ProcessUtils.class.getDeclaredConstructor();
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

    /**
     * Mock a process builder that has a non-zero exit value.
     * @return the mocked process builder
     * @throws InterruptedException if the process is interrupted
     * @throws IOException if the process cannot be started
     */
    private static ProcessBuilder mockFailingProcess()
        throws InterruptedException, IOException {
        Process process = mock(Process.class);
        when(process.waitFor()).thenReturn(1);

        ProcessBuilder processBuilder = mock(ProcessBuilder.class);
        when(processBuilder.start()).thenReturn(process);

        return processBuilder;
    }

    @Test
    void runProcessBuilderTest() throws InterruptedException, IOException {
        // given: a process that has a non-zero exit value
        ProcessBuilder processBuilder = mockFailingProcess();

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the process is started
            () -> runProcessBuilder(processBuilder, false)
        );
    }

    @Test
    void testRunProcessEnsureSuccessThrowsIOException() throws IOException {
        // mock the ProcessBuilder class
        ProcessBuilder mockedPb = mock(ProcessBuilder.class);

        // make the start method throw an IOException
        when(mockedPb.start()).thenThrow(new IOException("Test exception"));

        // call the runProcess method
        // and assert that a RuntimeException is thrown
        assertThrows(
            UncheckedIOException.class,
            () -> runProcessEnsureSuccess(mockedPb)
        );
    }

    @Test
    void testGetExitCodeThrowsInterruptedException()
        throws InterruptedException {
        // mock the Process class
        Process mockedProcess = mock(Process.class);

        // make the waitFor method throw an InterruptedException
        when(mockedProcess.waitFor())
            .thenThrow(new InterruptedException("Test exception"));

        // call the getExitCode method
        assertNotEquals(0, getExitCode(mockedProcess));
    }

    @Test
    void testRunProcessEnsureSuccessThrowsRuntimeException()
        throws InterruptedException, IOException {
        // mock the ProcessBuilder and Process classes
        ProcessBuilder mockedPb = mock(ProcessBuilder.class);
        Process mockedProcess = mock(Process.class);
        when(mockedPb.start()).thenReturn(mockedProcess);

        // make the waitFor method return a non-zero exit code
        when(mockedProcess.waitFor()).thenReturn(1);

        // call the runProcessEnsureSuccess method
        // and assert that a RuntimeException is thrown
        assertThrows(
            RuntimeException.class,
            () -> runProcessEnsureSuccess(mockedPb)
        );
    }

    @Test
    void collectOutputAsListEmpty() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("true");
        Process process = pb.start();
        List<String> output = collectOutputAsList(process);
        assertTrue(output.isEmpty());
    }

    @Test
    void collectOutputEmpty() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("true");
        Process process = pb.start();
        StringBuilder output = collectOutput(process);
        assertTrue(output.toString().isEmpty());
    }

    @Test
    void readLinesThrowingIOException() {
        // given reader that throws an IOException
        BufferedReader reader = mock(BufferedReader.class);
        try {
            when(reader.readLine())
                .thenThrow(new IOException("Test exception"));
        } catch (IOException e) {
            fail("IOException should not be thrown");
        }

        assertThrows(
            // then: an IOException should be thrown
            IOException.class,

            // when reading the reader
            () -> readLines(reader)
        );
    }

    @Test
    void testReadLines() throws IOException {
        BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine())
            .thenReturn("line1")
            .thenReturn("line2")
            .thenReturn(null);

        List<String> lines = readLines(reader);

        assertEquals(2, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
    }

    @Test
    void testEmptyReadLines() throws IOException {
        BufferedReader reader = mock(BufferedReader.class);
        when(reader.readLine()).thenReturn(null);

        List<String> lines = readLines(reader);

        assertTrue(lines.isEmpty());
    }

    @Test
    void testCollectOutputAsListExceptionOnClose() {
        Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(
            new ByteArrayInputStream("test".getBytes())
        );

        assertThrows(IOException.class, () -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            ) {
                @Override
                public void close() throws IOException {
                    throw new IOException("Test exception on close");
                }
            }
            ) {
                collectOutputAsList(process);
            }
        });
    }
}
