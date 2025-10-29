package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Simple panel to manage firewalld masquerading (NAT) per zone.
 *
 * Provides controls to enable/disable masquerade and to query the current state.
 */
public class MasqueradingPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JCheckBox permanentBox;
    private final JTextArea outputArea;

    public MasqueradingPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(1, 4, 6, 6));
        top.add(new JLabel("Zone:"));
        zoneBox = new JComboBox<>(new String[]{"public", "internal", "external", "dmz", "work", "home", "trusted"});
        top.add(zoneBox);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton enableBtn = new JButton("Enable Masquerade");
        JButton disableBtn = new JButton("Disable Masquerade");
        JButton queryBtn = new JButton("Query Masquerade");
        buttons.add(enableBtn);
        buttons.add(disableBtn);
        buttons.add(queryBtn);
        add(buttons, BorderLayout.CENTER);

        outputArea = new JTextArea(12, 60);
        outputArea.setEditable(false);
        JScrollPane sp = new JScrollPane(outputArea);
        add(sp, BorderLayout.SOUTH);

        enableBtn.addActionListener(this::onEnable);
        disableBtn.addActionListener(this::onDisable);
        queryBtn.addActionListener(this::onQuery);
    }

    private void onEnable(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        boolean perm = permanentBox.isSelected();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
    java.util.List<String> cmd = new java.util.ArrayList<>();
    cmd.add("firewall-cmd");
    if (perm) cmd.add("--permanent");
    cmd.add("--zone=" + zone);
    cmd.add("--add-masquerade");
    new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onDisable(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        boolean perm = permanentBox.isSelected();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
    java.util.List<String> cmd = new java.util.ArrayList<>();
    cmd.add("firewall-cmd");
    if (perm) cmd.add("--permanent");
    cmd.add("--zone=" + zone);
    cmd.add("--remove-masquerade");
    new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onQuery(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--query-masquerade");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
