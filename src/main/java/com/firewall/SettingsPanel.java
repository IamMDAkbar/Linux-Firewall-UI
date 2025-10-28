package com.firewall;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

/**
 * Basic settings panel with theme selection, background chooser and a toggle for Claude Sonnet 3.5.
 */
public class SettingsPanel extends JPanel {
    private final Preferences prefs = Preferences.userNodeForPackage(SettingsPanel.class);
    private final JCheckBox enableClaude = new JCheckBox("Enable Claude Sonnet 3.5 for all clients");
    private final JComboBox<String> themeChoice = new JComboBox<>(new String[]{"Dark", "Light"});
    private final JLabel bgPath = new JLabel("(none)");
    // Privilege helper components
    private final JLabel privilegeStatus = new JLabel("(unknown)");
    private final JLabel visudoLine = new JLabel();
    private final JButton testPrivilegeBtn = new JButton("Test Privilege");
    private final JButton copyVisudoBtn = new JButton("Copy visudo line");

    public SettingsPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Settings");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(Color.WHITE);

        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 10, 10, 10));

        enableClaude.setOpaque(false);
        enableClaude.setForeground(Color.WHITE);
        form.add(enableClaude);
        form.add(Box.createVerticalStrut(16));

        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeRow.setOpaque(false);
        themeRow.add(new JLabel("Theme:"));
        themeRow.add(themeChoice);
        form.add(themeRow);
        form.add(Box.createVerticalStrut(12));

        JPanel bgRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bgRow.setOpaque(false);
        JButton choose = new JButton("Choose Background...");
        choose.addActionListener(e -> chooseBackground());
        bgRow.add(choose);
        bgRow.add(bgPath);
        form.add(bgRow);
        form.add(Box.createVerticalStrut(20));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton save = new JButton("Save");
        save.addActionListener(e -> saveSettings());
        JButton apply = new JButton("Apply Theme Now");
        apply.addActionListener(e -> applyTheme());
        actions.add(save);
        actions.add(apply);

        form.add(actions);
    form.add(Box.createVerticalStrut(20));

    // Privilege setup helper
    JPanel privPanel = new JPanel();
    privPanel.setOpaque(false);
    privPanel.setLayout(new BoxLayout(privPanel, BoxLayout.Y_AXIS));
    JLabel privTitle = new JLabel("Privilege setup");
    privTitle.setForeground(new Color(210, 210, 210));
    privTitle.setFont(privTitle.getFont().deriveFont(Font.BOLD, 13f));
    privPanel.add(privTitle);
    privPanel.add(Box.createVerticalStrut(8));

    JPanel privRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    privRow.setOpaque(false);
    privRow.add(new JLabel("Sudo test:"));
    privilegeStatus.setForeground(new Color(200, 200, 200));
    privRow.add(privilegeStatus);
    testPrivilegeBtn.addActionListener(e -> runPrivilegeTest());
    privRow.add(testPrivilegeBtn);
    privPanel.add(privRow);

    JPanel visudoRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
    visudoRow.setOpaque(false);
    String path = detectFirewallCmdPath();
    String line = "%firewall-admins ALL=(root) NOPASSWD: " + path;
    visudoLine.setText(line);
    visudoLine.setForeground(new Color(180, 180, 180));
    visudoRow.add(new JLabel("visudo line:"));
    visudoRow.add(visudoLine);
    copyVisudoBtn.addActionListener(e -> copyToClipboard(visudoLine.getText()));
    visudoRow.add(copyVisudoBtn);
    privPanel.add(visudoRow);

    form.add(privPanel);

        add(form, BorderLayout.CENTER);

        loadSettings();
    }

    private void chooseBackground() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select background image");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            bgPath.setText(f.getAbsolutePath());
            prefs.put("background_path", f.getAbsolutePath());
        }
    }

    private void saveSettings() {
        prefs.putBoolean("enable_claude", enableClaude.isSelected());
        prefs.put("theme", (String) themeChoice.getSelectedItem());
        JOptionPane.showMessageDialog(this, "Settings saved.");
    }

    private void loadSettings() {
        boolean c = prefs.getBoolean("enable_claude", false);
        enableClaude.setSelected(c);
        String theme = prefs.get("theme", "Dark");
        themeChoice.setSelectedItem(theme);
        String bg = prefs.get("background_path", "");
        bgPath.setText(bg.isEmpty() ? "(none)" : bg);
    }

    private void applyTheme() {
        String theme = (String) themeChoice.getSelectedItem();
        try {
            if ("Light".equalsIgnoreCase(theme)) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
            SwingUtilities.updateComponentTreeUI(SwingUtilities.getWindowAncestor(this));
            JOptionPane.showMessageDialog(this, "Theme applied. You may need to re-open some windows for full effect.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to apply theme: " + ex.getMessage());
        }
    }

    private String detectFirewallCmdPath() {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", "firewall-cmd");
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String out = r.readLine();
            p.waitFor();
            if (out != null && !out.trim().isEmpty()) return out.trim();
        } catch (Exception ignored) {}
        // fallback common path
        return "/usr/bin/firewall-cmd";
    }

    private void runPrivilegeTest() {
        privilegeStatus.setText("Testing...");
        testPrivilegeBtn.setEnabled(false);
        new Thread(() -> {
            try {
                String path = detectFirewallCmdPath();
                ProcessBuilder pb = new ProcessBuilder("sudo", "-n", path, "--state");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                StringBuilder out = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) out.append(line).append('\n');
                int code = p.waitFor();
                final String result = out.toString().trim();
                SwingUtilities.invokeLater(() -> {
                    if (code == 0) {
                        privilegeStatus.setText("OK — sudo allowed (no password needed)");
                        privilegeStatus.setForeground(new Color(120, 200, 120));
                    } else {
                        privilegeStatus.setText("Requires password or not allowed");
                        privilegeStatus.setForeground(new Color(220, 120, 120));
                        // show a short dialog with output for diagnostics
                        JTextArea ta = new JTextArea(result);
                        ta.setEditable(false);
                        ta.setBackground(new Color(50, 53, 55));
                        ta.setForeground(new Color(220, 220, 220));
                        ta.setBorder(new EmptyBorder(8,8,8,8));
                        JScrollPane sp = new JScrollPane(ta);
                        sp.setPreferredSize(new Dimension(600, 200));
                        JOptionPane.showMessageDialog(this, sp, "Privilege test output", JOptionPane.INFORMATION_MESSAGE);
                    }
                    testPrivilegeBtn.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    privilegeStatus.setText("Error testing privilege");
                    privilegeStatus.setForeground(new Color(220, 120, 120));
                    testPrivilegeBtn.setEnabled(true);
                });
            }
        }).start();
    }

    private void copyToClipboard(String s) {
        try {
            StringSelection sel = new StringSelection(s);
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.setContents(sel, sel);
            JOptionPane.showMessageDialog(this, "visudo line copied to clipboard.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to copy: " + e.getMessage());
        }
    }
}
