package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Arrays;
import java.util.List;

public class BackendConfigPanel extends JPanel {
    private final JComboBox<String> backendComboBox;
    private final JTextArea statusArea;
    private final JCheckBox permanentCheckBox;
    private final CommandRunner commandRunner;
    private final JButton applyButton;
    private final JTextArea currentConfigArea;

    public BackendConfigPanel() {
        commandRunner = new CommandRunner();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        // Backend selection
        JLabel backendLabel = new JLabel("Firewall Backend:", SwingConstants.RIGHT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        controlPanel.add(backendLabel, gbc);

        String[] backends = {"nftables", "iptables"};
        backendComboBox = new JComboBox<>(backends);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        controlPanel.add(backendComboBox, gbc);

        // Current configuration display
        JLabel currentConfigLabel = new JLabel("Current Configuration:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        controlPanel.add(currentConfigLabel, gbc);

        currentConfigArea = new JTextArea(5, 40);
        currentConfigArea.setEditable(false);
        JScrollPane configScrollPane = new JScrollPane(currentConfigArea);
        configScrollPane.setBorder(BorderFactory.createEtchedBorder());
        gbc.gridy = 2;
        controlPanel.add(configScrollPane, gbc);

        // Permanent checkbox
        permanentCheckBox = new JCheckBox("Make changes permanent");
        gbc.gridy = 3;
        controlPanel.add(permanentCheckBox, gbc);

        // Apply button
        applyButton = new JButton("Apply Backend Change");
        applyButton.addActionListener(e -> applyBackendChange());
        gbc.gridy = 4;
        controlPanel.add(applyButton, gbc);

        // Status Area
        statusArea = new JTextArea(10, 40);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));

        add(controlPanel, BorderLayout.NORTH);
        add(statusScrollPane, BorderLayout.CENTER);

        // Initialize
        refreshCurrentConfig();
    }

    private void refreshCurrentConfig() {
        String backendOutput = String.valueOf(commandRunner.runCommand(
            Arrays.asList("firewall-cmd", "--get-backend"),
            null,
            false
        ));

        String configOutput = String.valueOf(commandRunner.runCommand(
            Arrays.asList("firewall-cmd", "--list-all"),
            null,
            false
        ));

        StringBuilder config = new StringBuilder();
        config.append("Current Backend: ").append(backendOutput.trim()).append("\n\n");
        config.append("Firewall Configuration:\n").append(configOutput.trim());

        currentConfigArea.setText(config.toString());
        backendComboBox.setSelectedItem(backendOutput.trim());
    }

    private void applyBackendChange() {
        String selectedBackend = (String) backendComboBox.getSelectedItem();
        if (selectedBackend == null) return;

        List<String> command = Arrays.asList(
            "firewall-cmd",
            permanentCheckBox.isSelected() ? "--permanent" : "",
            "--set-backend=" + selectedBackend
        );

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Changing the firewall backend may temporarily disrupt network connectivity.\n" +
            "Are you sure you want to continue?",
            "Confirm Backend Change",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            String output = String.valueOf(commandRunner.runCommand(command, statusArea, true));
            if (output != null && !output.isEmpty()) {
                if (permanentCheckBox.isSelected()) {
                    commandRunner.runCommand(Arrays.asList("firewall-cmd", "--reload"), statusArea, true);
                }
                refreshCurrentConfig();
                
                JOptionPane.showMessageDialog(
                    this,
                    "Backend change applied successfully.\nPlease verify your firewall rules are working as expected.",
                    "Backend Changed",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
}