package pbrg.webservices.utils;

import java.io.IOException;

public class TestUtils {
    public static boolean python3Installed() {
        ProcessBuilder pb = new ProcessBuilder("python3", "--version");
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            return false;
        }
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            return false;
        }
        return exitCode == 0;
    }
}
