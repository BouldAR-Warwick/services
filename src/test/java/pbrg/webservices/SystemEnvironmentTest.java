package pbrg.webservices;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class SystemEnvironmentTest {

    public static boolean python3Installed() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python3", "--version");
        Process process = pb.start();
        int exitCode = process.waitFor();
        return exitCode == 0;
    }
    @Test
    void testPython3Installation() throws IOException, InterruptedException {
        assertTrue(python3Installed());
    }
}
