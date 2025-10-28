package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Panel to list available firewalld services and allow enabling/disabling them in a zone.
 */
public class ServicesPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JTextArea outputArea;

    public ServicesPanel() {
        setLayout(new BorderLayout(6,6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,6));
        JButton listServices = new JButton("List Available Services");
        JButton listActive = new JButton("List Active Services (zone)");
        zoneBox = new JComboBox<>(new String[]{"public","internal","external","dmz","work","home","trusted"});
        JButton add = new JButton("Add Service to Zone");
        JButton remove = new JButton("Remove Service from Zone");

        top.add(listServices);
        top.add(listActive);
        top.add(new JLabel("Zone:"));
        top.add(zoneBox);
        top.add(add);
        top.add(remove);
        add(top, BorderLayout.NORTH);

        outputArea = new JTextArea(14,60);
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        listServices.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> outputArea.setText(""));
            List<String> cmd = Arrays.asList("firewall-cmd","--get-services");
            new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
        });

        listActive.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> outputArea.setText(""));
            String zone = (String) zoneBox.getSelectedItem();
            List<String> cmd = Arrays.asList("firewall-cmd","--zone="+zone,"--list-services");
            new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
        });

        add.addActionListener(e -> {
            String service = JOptionPane.showInputDialog(this, "Service name to add (e.g. http):");
            if (service == null || service.trim().isEmpty()) return;
            String zone = (String) zoneBox.getSelectedItem();
            List<String> cmd = Arrays.asList("firewall-cmd","--zone="+zone,"--add-service="+service);
            new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
        });

        remove.addActionListener(e -> {
            String service = JOptionPane.showInputDialog(this, "Service name to remove (e.g. http):");
            if (service == null || service.trim().isEmpty()) return;
            String zone = (String) zoneBox.getSelectedItem();
            List<String> cmd = Arrays.asList("firewall-cmd","--zone="+zone,"--remove-service="+service);
            new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
        });
    }
}
