package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Panel to create/list/remove firewalld "rich rules".
 *
 * This provides a simple builder UI that constructs a rich rule string compatible with
 * `firewall-cmd --add-rich-rule='RULE'` and streams output via CommandRunner.
 */
public class RichRulesPanel extends JPanel {
    private final JComboBox<String> zoneBox;
    private final JComboBox<String> familyBox;
    private final JTextField sourceField;
    private final JTextField destField;
    private final JTextField protoField;
    private final JTextField portField;
    private final JComboBox<String> actionBox;
    private final JCheckBox permanentBox;
    private final JTextArea outputArea;
    private final JTextArea previewArea;

    public RichRulesPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(4, 4, 6, 6));

        top.add(new JLabel("Zone:"));
        zoneBox = new JComboBox<>(new String[]{"public", "internal", "external", "dmz", "work", "home", "trusted"});
        top.add(zoneBox);

        top.add(new JLabel("Family:"));
        familyBox = new JComboBox<>(new String[]{"ipv4", "ipv6"});
        top.add(familyBox);

        top.add(new JLabel("Source (CIDR or addr):"));
        sourceField = new JTextField();
        top.add(sourceField);

        top.add(new JLabel("Destination (addr):"));
        destField = new JTextField();
        top.add(destField);

        top.add(new JLabel("Protocol (tcp/udp):"));
        protoField = new JTextField();
        top.add(protoField);

        top.add(new JLabel("Port (number or range):"));
        portField = new JTextField();
        top.add(portField);

        top.add(new JLabel("Action:"));
        actionBox = new JComboBox<>(new String[]{"accept", "reject", "drop"});
        top.add(actionBox);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        add(top, BorderLayout.NORTH);

        JPanel mid = new JPanel(new BorderLayout(6,6));
        JPanel midButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton addBtn = new JButton("Add Rich Rule");
        JButton removeBtn = new JButton("Remove Rich Rule");
        JButton listBtn = new JButton("List Rich Rules (zone)");
        JButton previewBtn = new JButton("Preview Rule");
        midButtons.add(addBtn);
        midButtons.add(removeBtn);
        midButtons.add(listBtn);
        midButtons.add(previewBtn);

        mid.add(midButtons, BorderLayout.NORTH);

        previewArea = new JTextArea(4, 60);
        previewArea.setEditable(false);
        previewArea.setBorder(BorderFactory.createTitledBorder("Rich rule preview (firewall-cmd friendly)"));
        mid.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        add(mid, BorderLayout.CENTER);

        outputArea = new JTextArea(12, 60);
        outputArea.setEditable(false);
        JScrollPane sp = new JScrollPane(outputArea);
        add(sp, BorderLayout.SOUTH);

        addBtn.addActionListener(this::onAdd);
        removeBtn.addActionListener(this::onRemove);
        listBtn.addActionListener(this::onList);
        previewBtn.addActionListener(e -> previewArea.setText(buildRichRule()));
    }

    private String buildRichRule() {
        StringBuilder sb = new StringBuilder();
        sb.append("rule ");
        String family = (String) familyBox.getSelectedItem();
        if (family != null && !family.isEmpty()) {
            sb.append("family=\"").append(family).append("\" ");
        }

        String source = sourceField.getText().trim();
        if (!source.isEmpty()) {
            sb.append("source address=\"").append(source).append("\" ");
        }

        String dest = destField.getText().trim();
        if (!dest.isEmpty()) {
            sb.append("destination address=\"").append(dest).append("\" ");
        }

        String proto = protoField.getText().trim();
        String port = portField.getText().trim();
        if (!proto.isEmpty() && !port.isEmpty()) {
            sb.append("port port=\"").append(port).append("\" protocol=\"").append(proto).append("\" ");
        } else if (!port.isEmpty()) {
            // if protocol not provided, include port alone (firewalld may require protocol but we'll still include)
            sb.append("port port=\"").append(port).append("\" ");
        }

        String action = (String) actionBox.getSelectedItem();
        if (action != null && !action.isEmpty()) {
            sb.append(action);
        }

        return sb.toString().trim();
    }

    private void onAdd(ActionEvent e) {
        String rule = buildRichRule();
        if (rule.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please build a valid rich rule (use Preview).");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--zone=" + (String) zoneBox.getSelectedItem());
        cmd.add("--add-rich-rule=" + rule);

        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onRemove(ActionEvent e) {
        String rule = buildRichRule();
        if (rule.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please build a valid rich rule to remove (use Preview).");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));

        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--zone=" + (String) zoneBox.getSelectedItem());
        cmd.add("--remove-rich-rule=" + rule);

        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onList(ActionEvent e) {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--zone=" + (String) zoneBox.getSelectedItem(), "--list-rich-rules");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
