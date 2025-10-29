package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoggingMonitoringPanel extends JPanel {
    private final JTextArea logArea;
    private final JComboBox<String> zoneComboBox;
    private final JComboBox<String> loggingLevelComboBox;
    private final JCheckBox permanentCheckBox;
    private Timer logRefreshTimer;
    private final CommandRunner commandRunner;

    private class LogMonitorTask extends TimerTask {
        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                int result = commandRunner.runCommand(
                        Arrays.asList("journalctl", "-u", "firewalld",
                                "--since", "30 seconds ago", "--no-pager"),
                        logArea, false);
                logArea.append("\n[Log Monitor] Command exited with code: " + result + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    public LoggingMonitoringPanel() {
        commandRunner = new CommandRunner();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        // Zone selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel zoneLabel = new JLabel("Zone:", SwingConstants.RIGHT);
        controlPanel.add(zoneLabel, gbc);

        gbc.gridx = 1;
        zoneComboBox = new JComboBox<>();
        refreshZones();
        controlPanel.add(zoneComboBox, gbc);

        // Logging level selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel loggingLabel = new JLabel("Logging Level:", SwingConstants.RIGHT);
        controlPanel.add(loggingLabel, gbc);

        gbc.gridx = 1;
        String[] levels = {"off", "emerg", "alert", "crit", "error", "warning", "notice", "info", "debug"};
        loggingLevelComboBox = new JComboBox<>(levels);
        controlPanel.add(loggingLevelComboBox, gbc);

        // Permanent checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        permanentCheckBox = new JCheckBox("Permanent");
        controlPanel.add(permanentCheckBox, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton setLoggingButton = new JButton("Set Logging");
        JButton queryLoggingButton = new JButton("Query Logging");
        JButton viewLogsButton = new JButton("View Recent Logs");
        JButton startMonitoringButton = new JButton("Start Monitoring");
        JButton stopMonitoringButton = new JButton("Stop Monitoring");

        buttonsPanel.add(setLoggingButton);
        buttonsPanel.add(queryLoggingButton);
        buttonsPanel.add(viewLogsButton);
        buttonsPanel.add(startMonitoringButton);
        buttonsPanel.add(stopMonitoringButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        controlPanel.add(buttonsPanel, gbc);

        // Log display area
        logArea = new JTextArea(20, 60);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        setLoggingButton.addActionListener(e -> setLogging());
        queryLoggingButton.addActionListener(e -> queryLogging());
        viewLogsButton.addActionListener(e -> viewLogs());
        startMonitoringButton.addActionListener(e -> startMonitoring());
        stopMonitoringButton.addActionListener(e -> stopMonitoring());
    }

    private void refreshZones() {
        int result = commandRunner.runCommand(
                Arrays.asList("firewall-cmd", "--get-zones"), logArea, false);
        logArea.append("\n[Zone Refresh] Command exited with code: " + result + "\n");
    }

    private void setLogging() {
        String zone = (String) zoneComboBox.getSelectedItem();
        String level = (String) loggingLevelComboBox.getSelectedItem();
        boolean permanent = permanentCheckBox.isSelected();

        List<String> command = new ArrayList<>();
        command.add("firewall-cmd");
        command.add("--zone=" + zone);
        command.add("--set-log-denied=" + level);
        if (permanent) command.add("--permanent");

        int result = commandRunner.runCommand(command, logArea, false);
        logArea.append("\nSetting logging level... (Exit code: " + result + ")\n");

        if (permanent) {
            int reloadResult = commandRunner.runCommand(
                    Arrays.asList("firewall-cmd", "--reload"), logArea, false);
            logArea.append("\nFirewall reloaded (Exit code: " + reloadResult + ")\n");
        }
    }

    private void queryLogging() {
        String zone = (String) zoneComboBox.getSelectedItem();
        int result = commandRunner.runCommand(
                Arrays.asList("firewall-cmd", "--zone=" + zone, "--get-log-denied"), logArea, false);
        logArea.append("\nQueried logging level (Exit code: " + result + ")\n");
    }

    private void viewLogs() {
        int result = commandRunner.runCommand(
                Arrays.asList("journalctl", "-u", "firewalld", "--since", "5 minutes ago", "--no-pager"),
                logArea, false);
        logArea.append("\nViewed recent logs (Exit code: " + result + ")\n");
    }

    private void startMonitoring() {
        if (logRefreshTimer != null) {
            logRefreshTimer.cancel();
        }
        logRefreshTimer = new Timer();
        long initialDelay = 0L;
        long period = 5000L;
        logRefreshTimer.scheduleAtFixedRate(new LogMonitorTask(), initialDelay, period);
        logArea.append("\nMonitoring started...\n");
    }

    private void stopMonitoring() {
        if (logRefreshTimer != null) {
            logRefreshTimer.cancel();
            logRefreshTimer = null;
        }
        logArea.append("\nMonitoring stopped.\n");
    }
}
