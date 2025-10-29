package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.util.Arrays;
import java.util.List;

public class LockdownPanel extends JPanel {
    private final JToggleButton lockdownToggle;
    private final JTextArea statusArea;
    private final JCheckBox permanentCheckBox;
    private final CommandRunner commandRunner;
    private final JComboBox<String> allowedServicesComboBox;
    private final DefaultListModel<String> allowedServicesModel;
    private final JList<String> allowedServicesList;

    public LockdownPanel() {
        commandRunner = new CommandRunner();
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Control Panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;

        // Lockdown Toggle
        lockdownToggle = new JToggleButton("Enable Lockdown Mode");
        lockdownToggle.addActionListener(e -> toggleLockdown());
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        controlPanel.add(lockdownToggle, gbc);

        // Permanent Checkbox
        permanentCheckBox = new JCheckBox("Make changes permanent");
        gbc.gridy = 1;
        controlPanel.add(permanentCheckBox, gbc);

        // Allowed Services Section
        JPanel servicesPanel = new JPanel(new BorderLayout(5, 5));
        servicesPanel.setBorder(BorderFactory.createTitledBorder("Allowed Services"));

        // Services Combobox for adding new services
        allowedServicesComboBox = new JComboBox<>();
        JButton addServiceButton = new JButton("Add Service");
        addServiceButton.addActionListener(e -> addAllowedService());

        JPanel addServicePanel = new JPanel(new BorderLayout());
        addServicePanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        addServicePanel.add(allowedServicesComboBox, BorderLayout.CENTER);
        addServicePanel.add(addServiceButton, BorderLayout.EAST);

        // List of currently allowed services
        allowedServicesModel = new DefaultListModel<>();
        allowedServicesList = new JList<>(allowedServicesModel);
        JScrollPane servicesScrollPane = new JScrollPane(allowedServicesList);

        JButton removeServiceButton = new JButton("Remove Selected");
        removeServiceButton.addActionListener(e -> removeSelectedServices());

        servicesPanel.add(addServicePanel, BorderLayout.NORTH);
        servicesPanel.add(servicesScrollPane, BorderLayout.CENTER);
        servicesPanel.add(removeServiceButton, BorderLayout.SOUTH);

        gbc.gridy = 2;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        controlPanel.add(servicesPanel, gbc);

        // Status Area
        statusArea = new JTextArea(10, 40);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));

        add(controlPanel, BorderLayout.NORTH);
        add(statusScrollPane, BorderLayout.CENTER);

        // Initialize
        refreshServicesComboBox();
        updateLockdownStatus();
    }

    private void toggleLockdown() {
        boolean enableLockdown = lockdownToggle.isSelected();
        List<String> command = Arrays.asList(
            "firewall-cmd",
            permanentCheckBox.isSelected() ? "--permanent" : "",
            enableLockdown ? "--lockdown-on" : "--lockdown-off"
        );

        String output = String.valueOf(commandRunner.runCommand(command, statusArea, true));
        if (output != null && !output.isEmpty()) {
            if (permanentCheckBox.isSelected()) {
                commandRunner.runCommand(Arrays.asList("firewall-cmd", "--reload"), statusArea, true);
            }
            updateLockdownStatus();
        }
    }

    private void updateLockdownStatus() {
        String output = String.valueOf(commandRunner.runCommand(
            Arrays.asList("firewall-cmd", "--query-lockdown"),
            statusArea,
            false
        ));
        
        boolean isLockdown = "yes".equalsIgnoreCase(output.trim());
        lockdownToggle.setSelected(isLockdown);
        lockdownToggle.setText(isLockdown ? "Disable Lockdown Mode" : "Enable Lockdown Mode");
        
        // Update allowed services list
        refreshAllowedServicesList();
    }

    private void refreshServicesComboBox() {
        String output = String.valueOf(commandRunner.runCommand(
            Arrays.asList("firewall-cmd", "--get-services"),
            null,
            false
       ));
        
        if (output != null && !output.isEmpty()) {
            allowedServicesComboBox.removeAllItems();
            String[] services = output.trim().split("\\s+");
            for (String service : services) {
                allowedServicesComboBox.addItem(service);
            }
        }
    }

    private void refreshAllowedServicesList() {
        String output = String.valueOf(commandRunner.runCommand(
            Arrays.asList("firewall-cmd", "--list-lockdown-whitelist-commands"),
            null,
            false
        ));
        
        allowedServicesModel.clear();
        if (output != null && !output.isEmpty()) {
            String[] services = output.trim().split("\\n");
            for (String service : services) {
                allowedServicesModel.addElement(service);
            }
        }
    }

    private void addAllowedService() {
        String selectedService = (String) allowedServicesComboBox.getSelectedItem();
        if (selectedService != null) {
            List<String> command = Arrays.asList(
                "firewall-cmd",
                permanentCheckBox.isSelected() ? "--permanent" : "",
                "--add-lockdown-whitelist-command=" + selectedService
            );

            String output = String.valueOf(commandRunner.runCommand(command, statusArea, true));
            if (output != null && !output.isEmpty()) {
                if (permanentCheckBox.isSelected()) {
                    commandRunner.runCommand(Arrays.asList("firewall-cmd", "--reload"), statusArea, true);
                }
                refreshAllowedServicesList();
            }
        }
    }

    private void removeSelectedServices() {
        List<String> selectedServices = allowedServicesList.getSelectedValuesList();
        for (String service : selectedServices) {
            List<String> command = Arrays.asList(
                "firewall-cmd",
                permanentCheckBox.isSelected() ? "--permanent" : "",
                "--remove-lockdown-whitelist-command=" + service
            );

            String output = String.valueOf(commandRunner.runCommand(command, statusArea, true));
            if (output != null && !output.isEmpty()) {
                if (permanentCheckBox.isSelected()) {
                    commandRunner.runCommand(Arrays.asList("firewall-cmd", "--reload"), statusArea, true);
                }
            }
        }
        refreshAllowedServicesList();
    }
}