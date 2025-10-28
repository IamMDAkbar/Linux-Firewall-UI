package com.firewall;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to run shell commands and stream output into a JTextArea.
 */
public class CommandRunner {
    /**
     * Run a command and append stdout/stderr lines to the provided JTextArea.
     * This method runs the process on a background thread and updates the text area on the EDT.
     *
     * @param commandParts command and arguments as list (e.g. ["firewall-cmd", "--list-all"]) 
     * @param outputArea   JTextArea to append output to (must be non-null)
     * @param usePkexec    if true, the command will be prefixed with pkexec to request elevation
     * @return process exit code (blocks until finished)
     */
    public static int runCommand(List<String> commandParts, JTextArea outputArea, boolean usePkexec) {
        List<String> cmd = new ArrayList<>();
        if (usePkexec) {
            cmd.add("pkexec");
        }
        cmd.addAll(commandParts);

        appendLine(outputArea, "$ " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        try {
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                appendLine(outputArea, line);
            }

            int code = p.waitFor();
            appendLine(outputArea, "\nProcess exited with code: " + code);
            return code;
        } catch (Exception e) {
            appendLine(outputArea, "Error running command: " + e.getMessage());
            return -1;
        }
    }

    private static void appendLine(JTextArea area, String line) {
        if (area == null) return;
        SwingUtilities.invokeLater(() -> {
            area.append(line + "\n");
            area.setCaretPosition(area.getDocument().getLength());
        });
    }
}
