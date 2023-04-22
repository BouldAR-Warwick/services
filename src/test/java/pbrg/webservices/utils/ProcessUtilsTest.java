package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.utils.ProcessUtils.collectOutput;
import static pbrg.webservices.utils.ProcessUtils.collectOutputAsList;
import static pbrg.webservices.utils.ProcessUtils.getExitCode;
import static pbrg.webservices.utils.ProcessUtils.runProcessBuilder;
import static pbrg.webservices.utils.ProcessUtils.runProcessEnsureSuccess;
import static pbrg.webservices.utils.RouteUtils.generateRouteMoonBoard;
import static pbrg.webservices.utils.RouteUtils.getPythonScriptsDir;
import static pbrg.webservices.utils.RouteUtils.plotHoldsOnImagePython;
import static pbrg.webservices.utils.RouteUtils.setPythonScriptsDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.json.JSONArray;
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
    void generateRouteMoonBoardFileDoesNotExist() {
        // given: a file path that does not exist
        String originalPath = getPythonScriptsDir();
        setPythonScriptsDir("/dev/null/");

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the file is checked
            () -> generateRouteMoonBoard(1)
        );

        // after: reset the path
        setPythonScriptsDir(originalPath);
    }

    @Test
    void plotHoldsOnImagePythonFileDoesNotExist() {
        // given: a file path that does not exist
        String originalPath = getPythonScriptsDir();
        setPythonScriptsDir("/dev/null/");

        assertThrows(
            // then: an exception should be thrown
            RuntimeException.class,

            // when: the file is checked
            () -> plotHoldsOnImagePython(
                mock(Integer.class),
                mock(String.class),
                mock(String.class),
                mock(String.class),
                mock(JSONArray.class)
            )
        );

        // after: reset the path
        setPythonScriptsDir(originalPath);
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
    void collectOutputEmpty () throws IOException {
        ProcessBuilder pb = new ProcessBuilder("true");
        Process process = pb.start();
        StringBuilder output = collectOutput(process);
        assertTrue(output.toString().isEmpty());
    }
}
