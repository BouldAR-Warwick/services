package pbrg.webservices.utils;

import java.io.IOException;

final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Check if python3 is installed on the system.
     * @return true if python3 is installed, false otherwise.
     */
    public static boolean python3Installed() {
        ProcessBuilder getPython3Version =
            new ProcessBuilder("python3", "--version");
        return runCommand(getPython3Version);
    }

    /**
     * Run a process.
     * @param pb process builder
     * @return true if the process exited with code 0, false otherwise.
     */
    static boolean runCommand(final ProcessBuilder pb) {
        Process process;
        try {
            process = pb.start();
        } catch (IOException ignored) {
            return false;
        }
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException ignored) {
            return false;
        }
        return exitCode == 0;
    }
}
