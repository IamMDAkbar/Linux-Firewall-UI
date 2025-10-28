package com.firewall;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Simple dashboard panel showing firewall status and basic counts.
 */
public class DashboardPanel extends JPanel {
    private final JLabel stateLabel = new JLabel("Loading...");
    private final JLabel zonesLabel = new JLabel("-");
    private final JLabel servicesLabel = new JLabel("-");
    private final JLabel portsLabel = new JLabel("-");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshStats());
        top.add(refresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 12));
        stats.setOpaque(false);

        stats.add(makeCard("Status", stateLabel));
        stats.add(makeCard("Active Zones", zonesLabel));
        stats.add(makeCard("Services", servicesLabel));
        stats.add(makeCard("Open Ports", portsLabel));

        add(stats, BorderLayout.CENTER);

        refreshStats();
    }

    private JPanel makeCard(String titleText, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(50, 53, 55));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel t = new JLabel(titleText);
        t.setForeground(new Color(200, 200, 200));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 12f));

        content.setForeground(new Color(230, 230, 230));
        content.setFont(content.getFont().deriveFont(Font.PLAIN, 18f));

        p.add(t, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void refreshStats() {
        stateLabel.setText("Refreshing...");
        zonesLabel.setText("Refreshing...");
        servicesLabel.setText("Refreshing...");
        portsLabel.setText("Refreshing...");

        new Thread(() -> {
            try {
                JTextArea ta1 = new JTextArea();
                CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--state"), ta1, false);
                String state = ta1.getText().trim();

                JTextArea ta2 = new JTextArea();
                CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--get-active-zones"), ta2, false);
                String zonesOut = ta2.getText().trim();

                JTextArea ta3 = new JTextArea();
                CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--list-services"), ta3, false);
                String servicesOut = ta3.getText().trim();

                JTextArea ta4 = new JTextArea();
                CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--list-ports"), ta4, false);
                String portsOut = ta4.getText().trim();

                final String fstate = state.isEmpty() ? "Unknown" : state;
                final String fzones = countNonEmptyLines(zonesOut);
                final String fservices = countNonEmptyTokens(servicesOut);
                final String fports = countNonEmptyTokens(portsOut);

                SwingUtilities.invokeLater(() -> {
                    stateLabel.setText(fstate);
                    zonesLabel.setText(fzones);
                    servicesLabel.setText(fservices);
                    portsLabel.setText(fports);
                });
            } catch (Throwable t) {
                SwingUtilities.invokeLater(() -> stateLabel.setText("Error"));
            }
        }).start();
    }

    private String countNonEmptyLines(String s) {
        if (s == null || s.isEmpty()) return "0";
        String[] lines = s.split("\\r?\\n");
        int count = 0;
        for (String l : lines) if (!l.trim().isEmpty()) count++;
        return String.valueOf(count);
    }

    private String countNonEmptyTokens(String s) {
        if (s == null || s.isEmpty()) return "0";
        String[] toks = s.split("\\s+");
        int count = 0;
        for (String t : toks) if (!t.trim().isEmpty()) count++;
        return String.valueOf(count);
    }
}
