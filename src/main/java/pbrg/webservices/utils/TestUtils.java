package pbrg.webservices.utils;

import java.io.IOException;

public final class TestUtils {

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
     * Check if the Docker daemon is running.
     * @return true if the Docker daemon in running, false otherwise.
     */
    public static boolean dockerDaemonRunning() {
        ProcessBuilder checkDockerDaemonCommand =
            new ProcessBuilder("docker", "stats", "--no-stream");
        return runCommand(checkDockerDaemonCommand);
    }

    /**
     * Run a process (note, from a thread - this is blocking).
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
