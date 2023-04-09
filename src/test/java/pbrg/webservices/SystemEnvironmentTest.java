package pbrg.webservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class SystemEnvironmentTest {

    @Test
    void testPython3Installation() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python3", "--version");
        Process process = pb.start();
        int exitCode = process.waitFor();
        assertEquals(0, exitCode);
    }
}
