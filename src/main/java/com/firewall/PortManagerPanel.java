package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Panel for adding/removing/listing firewall ports.
 */
public class PortManagerPanel extends JPanel {
    private final JTextField portField;
    private final JComboBox<String> protoBox;
    private final JComboBox<String> zoneBox;
    private final JCheckBox permanentBox;
    private final JTextArea outputArea;

    public PortManagerPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(2, 4, 6, 6));
        top.add(new JLabel("Port:"));
        portField = new JTextField();
        top.add(portField);

        top.add(new JLabel("Protocol:"));
        protoBox = new JComboBox<>(new String[]{"tcp", "udp"});
        top.add(protoBox);

        top.add(new JLabel("Zone:"));
        zoneBox = new JComboBox<>(new String[]{"public", "internal", "external", "dmz", "work", "home", "trusted"});
        top.add(zoneBox);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton addBtn = new JButton("Add Port");
        JButton removeBtn = new JButton("Remove Port");
        JButton listBtn = new JButton("List Ports (zone)");
        buttons.add(addBtn);
        buttons.add(removeBtn);
        buttons.add(listBtn);
        add(buttons, BorderLayout.CENTER);

        outputArea = new JTextArea(12, 60);
        outputArea.setEditable(false);
        JScrollPane sp = new JScrollPane(outputArea);
        add(sp, BorderLayout.SOUTH);

        addBtn.addActionListener(this::onAdd);
        removeBtn.addActionListener(this::onRemove);
        listBtn.addActionListener(this::onList);
    }

    private void onAdd(ActionEvent e) {
        String port = portField.getText().trim();
        String proto = (String) protoBox.getSelectedItem();
        String zone = (String) zoneBox.getSelectedItem();
        boolean perm = permanentBox.isSelected();

        if (port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a port or port range (e.g. 8080 or 8000-8100)");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        final List<String> cmdFinal;
        if (perm) {
            cmdFinal = Arrays.asList("firewall-cmd", "--permanent", "--zone=" + zone, "--add-port=" + port + "/" + proto);
        } else {
            cmdFinal = Arrays.asList("firewall-cmd", "--zone=" + zone, "--add-port=" + port + "/" + proto);
        }

        // run asynchronously so UI remains responsive
        new Thread(() -> CommandRunner.runCommand(cmdFinal, outputArea, true)).start();
    }

    private void onRemove(ActionEvent e) {
        String port = portField.getText().trim();
        String proto = (String) protoBox.getSelectedItem();
        String zone = (String) zoneBox.getSelectedItem();
        boolean perm = permanentBox.isSelected();

        if (port.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a port to remove.");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        final List<String> cmdFinal;
        if (perm) {
            cmdFinal = Arrays.asList("firewall-cmd", "--permanent", "--zone=" + zone, "--remove-port=" + port + "/" + proto);
        } else {
            cmdFinal = Arrays.asList("firewall-cmd", "--zone=" + zone, "--remove-port=" + port + "/" + proto);
        }

        new Thread(() -> CommandRunner.runCommand(cmdFinal, outputArea, true)).start();
    }

    private void onList(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--list-ports");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
