package pbrg.webservices.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pbrg.webservices.utils.ProcessUtils.runProcessBuilder;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ProcessUtilsTest {

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
}
