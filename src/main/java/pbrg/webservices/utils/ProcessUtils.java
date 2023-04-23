package pbrg.webservices.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class ProcessUtils {

    /** Static class, no need to instantiate. */
    private ProcessUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Run the process builder command.
     * @param command The command to run
     */
    public static void runProcessBuilder(
        final ProcessBuilder command
    ) {
        runProcessBuilder(command, false);
    }

    /**
     * Run the process builder command.
     * @param command The command to run
     * @param collectOutput Whether to collect the output of the command
     * @return The output of the command
     */
    public static List<String> runProcessBuilder(
        final @NotNull ProcessBuilder command,
        final boolean collectOutput
    ) {
        List<String> output = null;
        try {
            Process process = command.start();
            if (collectOutput) {
                output = collectOutputAsList(process);
            }
            int exitCode = process.waitFor();
            process.destroy();
            if (exitCode != 0) {
                throw new InterruptedException(
                    "Command failed (non-zero exit code)"
                );
            }
            return output;
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Run a process, throwing a RuntimeException if the process fails.
     * @param pb process builder
     * @return process
     */
    static Process runProcessEnsureSuccess(@NotNull final ProcessBuilder pb) {
        // read the output printed by the python script
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        // ensure success
        int exitCode = getExitCode(process);
        if (exitCode != 0) {
            throw new RuntimeException(
                "Route thumbnail generation failed with exit code " + exitCode
            );
        }
        return process;
    }

    /**
     * Run a process, ensure success, and return output as a StringBuilder.
     * @param pb process builder
     * @return output as a string builder, each line is a new line
     */
    static StringBuilder runProcessGetOutputEnsureSuccess(
        @NotNull final ProcessBuilder pb
    ) throws IOException {
        Process process = runProcessEnsureSuccess(pb);
        StringBuilder output = collectOutput(process);
        return output;
    }

    /**
     * Collect output from a process.
     * @param process process
     * @return output as a string builder, each line is a new line
     */
    static @NotNull StringBuilder collectOutput(
        @NotNull final Process process
    ) throws IOException {
        StringBuilder output = new StringBuilder();
        for (String line : collectOutputAsList(process)) {
            output.append(line);
            output.append(System.lineSeparator());
        }
        return output;
    }

    /**
     * Collect output from a process in a list of strings.
     * @param process process
     * @return output as a list of strings, each string is a line
     */
    public static @NotNull List<String> collectOutputAsList(
        final @NotNull Process process
    ) throws IOException {
        List<String> output;

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            )
        ) {
            output = readLines(reader);
        }
        return output;
    }

    /**
     * Read lines from a buffered reader.
     * @param reader buffered reader
     * @return list of lines
     * @throws IOException if an I/O error occurs
     */
    static @NotNull List<String> readLines(
        final @NotNull BufferedReader reader
    ) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    /**
     * Get exit code from a process.
     * @param process process
     * @return exit code
     */
    static int getExitCode(final Process process) {
        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            return -1;
        }
        return exitCode;
    }
}
