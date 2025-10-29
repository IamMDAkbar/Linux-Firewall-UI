package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Panel to manage ICMP settings via firewalld.
 *
 * Provides simple controls to block/unblock ICMP types per zone and list/query configured ICMP blocks.
 */
public class ICMPControlPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JComboBox<String> icmpTypeBox;
    private final JCheckBox permanentBox;
    private final JTextArea outputArea;

    public ICMPControlPanel() {
        setLayout(new BorderLayout(8,8));

        JPanel top = new JPanel(new GridLayout(2,4,6,6));
        top.add(new JLabel("Zone:"));
        zoneBox = new JComboBox<>(new String[]{"public","internal","external","dmz","work","home","trusted"});
        top.add(zoneBox);

        top.add(new JLabel("ICMP Type:"));
        icmpTypeBox = new JComboBox<>(new String[]{
            "echo-request", "echo-reply", "destination-unreachable", "time-exceeded", "parameter-problem", "address-mask-request"
        });
        icmpTypeBox.setEditable(true); // allow custom types
        top.add(icmpTypeBox);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT,8,6));
        JButton addBtn = new JButton("Block ICMP Type");
        JButton removeBtn = new JButton("Unblock ICMP Type");
        JButton listBtn = new JButton("List ICMP Blocks (zone)");
        JButton queryBtn = new JButton("Query ICMP Block");
        buttons.add(addBtn);
        buttons.add(removeBtn);
        buttons.add(listBtn);
        buttons.add(queryBtn);
        add(buttons, BorderLayout.CENTER);

        outputArea = new JTextArea(12,60);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        addBtn.addActionListener(this::onAdd);
        removeBtn.addActionListener(this::onRemove);
        listBtn.addActionListener(this::onList);
        queryBtn.addActionListener(this::onQuery);
    }

    private void onAdd(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        String type = ((String) icmpTypeBox.getSelectedItem()).trim();
        boolean perm = permanentBox.isSelected();

        if (type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an ICMP type to block (e.g. echo-request).");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (perm) cmd.add("--permanent");
        cmd.add("--zone=" + zone);
        cmd.add("--add-icmp-block=" + type);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onRemove(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        String type = ((String) icmpTypeBox.getSelectedItem()).trim();
        boolean perm = permanentBox.isSelected();

        if (type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an ICMP type to unblock.");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (perm) cmd.add("--permanent");
        cmd.add("--zone=" + zone);
        cmd.add("--remove-icmp-block=" + type);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onList(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--list-icmp-blocks");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onQuery(ActionEvent e) {
        String zone = (String) zoneBox.getSelectedItem();
        String type = ((String) icmpTypeBox.getSelectedItem()).trim();
        if (type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an ICMP type to query.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + zone, "--query-icmp-block=" + type);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
