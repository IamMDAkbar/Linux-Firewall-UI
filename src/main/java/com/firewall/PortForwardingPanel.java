package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Panel to add/remove/list firewall port forwarding rules (forward-port).
 *
 * Uses firewall-cmd --add-forward-port / --remove-forward-port / --list-forward-ports
 */
public class PortForwardingPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JTextField portFromField;
    private final JTextField portToField;
    private final JTextField toAddrField;
    private final JComboBox<String> protoBox;
    private final JCheckBox permanentBox;
    private final JTextArea outputArea;

    public PortForwardingPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(3, 4, 6, 6));
        top.add(new JLabel("Zone:"));
        zoneBox = new JComboBox<>(new String[]{"public", "internal", "external", "dmz", "work", "home", "trusted"});
        top.add(zoneBox);

        top.add(new JLabel("Protocol:"));
        protoBox = new JComboBox<>(new String[]{"tcp", "udp"});
        top.add(protoBox);

        top.add(new JLabel("Port (from):"));
        portFromField = new JTextField();
        top.add(portFromField);

        top.add(new JLabel("Port (to):"));
        portToField = new JTextField();
        top.add(portToField);

        top.add(new JLabel("Destination address (optional):"));
        toAddrField = new JTextField();
        top.add(toAddrField);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton addBtn = new JButton("Add Forwarding");
        JButton removeBtn = new JButton("Remove Forwarding");
        JButton listBtn = new JButton("List Forward Ports (zone)");
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
        String from = portFromField.getText().trim();
        String to = portToField.getText().trim();
        String proto = (String) protoBox.getSelectedItem();
        String toaddr = toAddrField.getText().trim();
        String zone = (String) zoneBox.getSelectedItem();

        if (from.isEmpty() || to.isEmpty() || proto == null || proto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide from/to ports and protocol.");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        String forwardSpec = "port=\"" + from + "\":proto=\"" + proto + "\":toport=\"" + to + "\"";
        if (!toaddr.isEmpty()) {
            forwardSpec += ":toaddr=\"" + toaddr + "\"";
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--zone=" + zone);
        cmd.add("--add-forward-port=" + forwardSpec);

        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onRemove(ActionEvent e) {
        String from = portFromField.getText().trim();
        String to = portToField.getText().trim();
        String proto = (String) protoBox.getSelectedItem();
        String toaddr = toAddrField.getText().trim();
        String zone = (String) zoneBox.getSelectedItem();

        if (from.isEmpty() || to.isEmpty() || proto == null || proto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide from/to ports and protocol to remove a forwarding rule.");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        String forwardSpec = "port=\"" + from + "\":proto=\"" + proto + "\":toport=\"" + to + "\"";
        if (!toaddr.isEmpty()) {
            forwardSpec += ":toaddr=\"" + toaddr + "\"";
        }

        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--zone=" + zone);
        cmd.add("--remove-forward-port=" + forwardSpec);

        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onList(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--list-forward-ports");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
