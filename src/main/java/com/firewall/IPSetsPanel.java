package com.firewall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Panel to manage firewalld IP sets using firewall-cmd.
 *
 * Supported operations (via UI):
 * - list ipsets
 * - create/delete ipset
 * - info ipset
 * - add/remove/query/list entries in an ipset
 *
 * The panel uses the existing CommandRunner to execute firewall-cmd and
 * stream output to the UI. Use the "Permanent" checkbox to operate on permanent
 * configuration (adds --permanent flag).
 */
public class IPSetsPanel extends JPanel {
    private final JTextField nameField;
    private final JComboBox<String> typeBox;
    private final JComboBox<String> familyBox;
    private final JCheckBox permanentBox;
    private final JTextField entryField;
    private final JTextArea outputArea;

    public IPSetsPanel() {
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new GridLayout(3, 4, 6, 6));

        top.add(new JLabel("IPSet name:"));
        nameField = new JTextField();
        top.add(nameField);

        top.add(new JLabel("Type:"));
        typeBox = new JComboBox<>(new String[]{"hash:ip", "hash:net", "list:set", "bitmap:ip", "hash:ip,port"});
        top.add(typeBox);

        top.add(new JLabel("Family:"));
        familyBox = new JComboBox<>(new String[]{"inet", "inet6"});
        top.add(familyBox);

        top.add(new JLabel("Permanent:"));
        permanentBox = new JCheckBox();
        top.add(permanentBox);

        top.add(new JLabel("Entry (IP/CIDR/etc):"));
        entryField = new JTextField();
        top.add(entryField);

        add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton listBtn = new JButton("List IPSets");
        JButton infoBtn = new JButton("Info IPSet");
        JButton createBtn = new JButton("Create IPSet");
        JButton deleteBtn = new JButton("Delete IPSet");
        JButton addEntryBtn = new JButton("Add Entry");
        JButton removeEntryBtn = new JButton("Remove Entry");
        JButton listEntriesBtn = new JButton("List Entries");
        JButton queryEntryBtn = new JButton("Query Entry");
        JButton typesBtn = new JButton("Get IPSet Types");

        buttons.add(listBtn);
        buttons.add(infoBtn);
        buttons.add(createBtn);
        buttons.add(deleteBtn);
        buttons.add(addEntryBtn);
        buttons.add(removeEntryBtn);
        buttons.add(listEntriesBtn);
        buttons.add(queryEntryBtn);
        buttons.add(typesBtn);

        add(buttons, BorderLayout.CENTER);

        outputArea = new JTextArea(12, 60);
        outputArea.setEditable(false);
        JScrollPane sp = new JScrollPane(outputArea);
        add(sp, BorderLayout.SOUTH);

        // action bindings
        listBtn.addActionListener(this::onListIPSets);
        typesBtn.addActionListener(this::onGetTypes);
        createBtn.addActionListener(this::onCreate);
        deleteBtn.addActionListener(this::onDelete);
        infoBtn.addActionListener(this::onInfo);
        addEntryBtn.addActionListener(this::onAddEntry);
        removeEntryBtn.addActionListener(this::onRemoveEntry);
        listEntriesBtn.addActionListener(this::onListEntries);
        queryEntryBtn.addActionListener(this::onQueryEntry);
    }

    private void onListIPSets(ActionEvent e) {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--get-ipsets");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onGetTypes(ActionEvent e) {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = Arrays.asList("firewall-cmd", "--get-ipset-types");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onCreate(ActionEvent e) {
        String name = nameField.getText().trim();
        String type = (String) typeBox.getSelectedItem();
        String family = (String) familyBox.getSelectedItem();
        if (name.isEmpty() || type == null || type.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a name and type for the ipset.");
            return;
        }

        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--new-ipset=" + name);
        cmd.add("--type=" + type);
        if (family != null && !family.isEmpty()) cmd.add("--family=" + family);

        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onDelete(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify the ipset name to delete.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--delete-ipset=" + name);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onInfo(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify the ipset name to query.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--info-ipset=" + name);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onAddEntry(ActionEvent e) {
        String name = nameField.getText().trim();
        String entry = entryField.getText().trim();
        if (name.isEmpty() || entry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify ipset name and an entry to add.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--ipset=" + name);
        cmd.add("--add-entry=" + entry);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onRemoveEntry(ActionEvent e) {
        String name = nameField.getText().trim();
        String entry = entryField.getText().trim();
        if (name.isEmpty() || entry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify ipset name and an entry to remove.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--ipset=" + name);
        cmd.add("--remove-entry=" + entry);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, true)).start();
    }

    private void onListEntries(ActionEvent e) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify ipset name to list entries.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--ipset=" + name);
        cmd.add("--get-entries");
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }

    private void onQueryEntry(ActionEvent e) {
        String name = nameField.getText().trim();
        String entry = entryField.getText().trim();
        if (name.isEmpty() || entry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify ipset name and an entry to query.");
            return;
        }
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
        List<String> cmd = new ArrayList<>();
        cmd.add("firewall-cmd");
        if (permanentBox.isSelected()) cmd.add("--permanent");
        cmd.add("--ipset=" + name);
        cmd.add("--query-entry=" + entry);
        new Thread(() -> CommandRunner.runCommand(cmd, outputArea, false)).start();
    }
}
