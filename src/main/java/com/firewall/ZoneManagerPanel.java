package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Simple zone manager UI: list zones, set default zone, show zone details.
 */
public class ZoneManagerPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JTextArea outputArea;

    public ZoneManagerPanel() {
        setLayout(new BorderLayout(6, 6));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton listZones = new JButton("List Zones");
        zoneBox = new JComboBox<>(new String[]{"public", "internal", "external", "dmz", "work", "home", "trusted"});
        JButton detailsBtn = new JButton("Show Zone Details");
        JButton setDefaultBtn = new JButton("Set Default Zone");

        top.add(listZones);
        top.add(new JLabel("Zone:"));
        top.add(zoneBox);
        top.add(detailsBtn);
        top.add(setDefaultBtn);
        add(top, BorderLayout.NORTH);

        outputArea = new JTextArea(14, 60);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        listZones.addActionListener(this::onListZones);
        detailsBtn.addActionListener(this::onDetails);
        setDefaultBtn.addActionListener(this::onSetDefault);
    }

    private void onListZones(ActionEvent e) {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--get-zones");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onDetails(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--list-all");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onSetDefault(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        int confirmed = JOptionPane.showConfirmDialog(this, "Set '" + zone + "' as default zone?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirmed != JOptionPane.YES_OPTION) return;
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--set-default-zone=" + zone);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }
}
