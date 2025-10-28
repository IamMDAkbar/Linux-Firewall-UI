package com.firewall;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Documentation panel that loads content from `resources/docs.md` when available.
 */
public class DocumentationPanel extends JPanel {
    public DocumentationPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Documentation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);

        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        ta.setBackground(new Color(43, 43, 43));
        ta.setForeground(new Color(220, 220, 220));
        ta.setFont(new Font("Monospace", Font.PLAIN, 13));
        ta.setBorder(new EmptyBorder(12,12,12,12));

        String content = loadResourceDocs();
        ta.setText(content);

        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
    }

    private String loadResourceDocs() {
        InputStream is = getClass().getResourceAsStream("/docs.md");
        if (is == null) {
            return "Documentation not found.\n";
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            return "Failed to load documentation: " + e.getMessage();
        }
        return sb.toString();
    }
}
