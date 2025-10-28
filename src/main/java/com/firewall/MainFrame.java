package com.firewall;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;

/**
 * Main application frame with a professional, modern UI design.
 * Features a sidebar navigation and categorized feature sections.
 */
public class MainFrame extends JFrame {
    private final JLabel statusLabel = new JLabel("Checking status...");
    private final JLabel backendLabel = new JLabel("Detecting backend...");
    private JPanel contentPanel;
    private JPanel centerCards;
    private Color accentColor = new Color(82, 145, 255);
    private Color cardBackground = new Color(50, 53, 55);
    private Color hoverColor = new Color(60, 63, 65);

    public MainFrame() {
        setTitle("Kali Firewall Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1400, 850);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1200, 700));

        // Modern window styling
        getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(43, 43, 43));
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setBackground(new Color(43, 43, 43));
        
        // Create sidebar
        JPanel sidebar = createSidebar();
        mainContainer.add(sidebar, BorderLayout.WEST);
        
        // Create content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(43, 43, 43));
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        JPanel contentWrapper = new JPanel(new BorderLayout(0, 25));
        contentWrapper.setOpaque(false);
        
        // Header
        contentWrapper.add(createHeader(), BorderLayout.NORTH);
        
    // Main content with categorized sections (card layout to swap views)
    centerCards = new JPanel(new CardLayout());
    centerCards.setOpaque(false);
    centerCards.add(createMainContent(), "home");
    centerCards.add(new DashboardPanel(), "dashboard");
    centerCards.add(new SettingsPanel(), "settings");
    centerCards.add(new DocumentationPanel(), "docs");
    contentWrapper.add(centerCards, BorderLayout.CENTER);
        
        // Status bar at bottom
        contentWrapper.add(createStatusBar(), BorderLayout.SOUTH);
        
        contentPanel.add(contentWrapper);
        mainContainer.add(contentPanel, BorderLayout.CENTER);
        
        add(mainContainer);
        
        // Initial status check
        checkStatus();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(37, 37, 38));
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setBorder(new EmptyBorder(25, 20, 25, 20));
        
        // Logo/Title area
        JPanel logoPanel = new JPanel(new BorderLayout(12, 0));
        logoPanel.setOpaque(false);
        logoPanel.setMaximumSize(new Dimension(240, 60));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel iconLabel = new JLabel("🛡️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        
        JLabel title = new JLabel("Firewall Manager");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitle = new JLabel("Security Control");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 11f));
        subtitle.setForeground(new Color(150, 150, 150));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        titleStack.add(title);
        titleStack.add(Box.createVerticalStrut(2));
        titleStack.add(subtitle);
        
        logoPanel.add(iconLabel, BorderLayout.WEST);
        logoPanel.add(titleStack, BorderLayout.CENTER);
        
        sidebar.add(logoPanel);
        sidebar.add(Box.createVerticalStrut(35));
        
        // Quick Actions section
        sidebar.add(createSidebarSection("Quick Actions"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSidebarButton("🔄 Reload Firewall", e -> onReload(e)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("📊 Show Status", e -> onShowStatus(e)));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createSidebarButton("🗂️ Active Zones", e -> onShowZones(e)));
        
        sidebar.add(Box.createVerticalStrut(30));
        
    // Navigation section
        sidebar.add(createSidebarSection("Navigation"));
        sidebar.add(Box.createVerticalStrut(10));
    // Home button to return to main content
    sidebar.add(createSidebarButton("🏡 Home", e -> showCard("home")));
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(createSidebarButton("🏠 Dashboard", e -> showCard("dashboard")));
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(createSidebarButton("⚙️ Settings", e -> showCard("settings")));
    sidebar.add(Box.createVerticalStrut(8));
    sidebar.add(createSidebarButton("📖 Documentation", e -> showCard("docs")));
        
        sidebar.add(Box.createVerticalGlue());
        
        // Bottom info
        JPanel versionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        versionPanel.setOpaque(false);
        versionPanel.setMaximumSize(new Dimension(240, 30));
        versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(versionLabel.getFont().deriveFont(Font.PLAIN, 10f));
        versionLabel.setForeground(new Color(120, 120, 120));
        versionPanel.add(versionLabel);
        
        sidebar.add(versionPanel);
        
        return sidebar;
    }
    
    private JLabel createSidebarSection(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setForeground(new Color(120, 120, 120));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(240, 20));
        return label;
    }
    
    private JButton createSidebarButton(String text, java.util.function.Consumer<ActionEvent> action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2d.setColor(new Color(50, 50, 51));
                    g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                
                super.paintComponent(g2d);
                g2d.dispose();
            }
        };
        
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 13f));
        btn.setForeground(new Color(200, 200, 200));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(240, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        
        btn.addActionListener(action::accept);
        
        return btn;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Firewall Configuration");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Manage your system's security settings and network rules");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 14f));
        subtitleLabel.setForeground(new Color(160, 160, 160));
        
        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);
        titleStack.add(titleLabel);
        titleStack.add(Box.createVerticalStrut(5));
        titleStack.add(subtitleLabel);
        
        header.add(titleStack, BorderLayout.WEST);
        
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        
        // Network Configuration Section
        content.add(createFeatureSection("Network Configuration", 
            new FeatureCard("Zone Management", "Configure security zones and assign network interfaces", 
                "🌐", e -> openWindow("Zone Management", new ZoneManagerPanel())),
            new FeatureCard("Services Control", "Manage allowed services and protocols", 
                "🔧", e -> openWindow("Services Control", new ServicesPanel())),
            new FeatureCard("Port Management", "Control port access and visibility", 
                "🔌", e -> openWindow("Port Management", new PortManagerPanel()))
        ));
        
        content.add(Box.createVerticalStrut(25));
        
        // Advanced Rules Section
        content.add(createFeatureSection("Advanced Rules",
            new FeatureCard("Rich Rules", "Create complex firewall rules with detailed conditions", 
                "📋", e -> openWindow("Rich Rules", makePlaceholder("Rich Rules"))),
            new FeatureCard("Port Forwarding", "Configure port forwarding and redirection", 
                "↔️", e -> openWindow("Port Forwarding", makePlaceholder("Port Forwarding"))),
            new FeatureCard("Masquerading & NAT", "Set up network address translation", 
                "🔀", e -> openWindow("Masquerading & NAT", makePlaceholder("Masquerading & NAT")))
        ));
        
        content.add(Box.createVerticalStrut(25));
        
        // System & Monitoring Section
        content.add(createFeatureSection("System & Monitoring",
            new FeatureCard("ICMP Control", "Manage ICMP protocol settings", 
                "📡", e -> openWindow("ICMP Control", makePlaceholder("ICMP Control"))),
            new FeatureCard("IP Sets", "Define and manage IP address sets", 
                "📝", e -> openWindow("IP Sets", makePlaceholder("IP Sets"))),
            new FeatureCard("Logging & Monitoring", "View firewall logs and activity", 
                "📊", e -> openWindow("Logging & Monitoring", makePlaceholder("Logging & Monitoring")))
        ));
        
        content.add(Box.createVerticalStrut(25));
        
        // Configuration Management Section
        content.add(createFeatureSection("Configuration Management",
            new FeatureCard("Lockdown Mode", "Enable enhanced security restrictions", 
                "🔒", e -> openWindow("Lockdown Mode", makePlaceholder("Lockdown Mode"))),
            new FeatureCard("Runtime/Permanent", "Toggle between runtime and permanent settings", 
                "⚡", e -> openWindow("Runtime/Permanent", makePlaceholder("Runtime/Permanent Toggle"))),
            new FeatureCard("Backend Configuration", "View backend info and default zone", 
                "⚙️", e -> openWindow("Backend & Default Zone", makePlaceholder("Backend & Default Zone")))
        ));
        
        content.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane);
        
        return wrapper;
    }

    private JPanel createFeatureSection(String title, FeatureCard... cards) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(sectionTitle.getFont().deriveFont(Font.BOLD, 16f));
        sectionTitle.setForeground(new Color(220, 220, 220));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        section.add(sectionTitle);
        section.add(Box.createVerticalStrut(15));
        
        JPanel cardsPanel = new JPanel(new GridLayout(1, cards.length, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        
        for (FeatureCard card : cards) {
            cardsPanel.add(createFeatureCard(card));
        }
        
        section.add(cardsPanel);
        
        return section;
    }

    private JPanel createFeatureCard(FeatureCard card) {
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(cardBackground);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(new Color(70, 70, 70));
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2d.dispose();
            }
        };
        
        cardPanel.setLayout(new BorderLayout(15, 10));
        cardPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Icon
        JLabel iconLabel = new JLabel(card.icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        
        // Text content
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(card.title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea descLabel = new JTextArea(card.description);
        descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN, 12f));
        descLabel.setForeground(new Color(160, 160, 160));
        descLabel.setLineWrap(true);
        descLabel.setWrapStyleWord(true);
        descLabel.setEditable(false);
        descLabel.setOpaque(false);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(descLabel);
        
        cardPanel.add(iconLabel, BorderLayout.WEST);
        cardPanel.add(textPanel, BorderLayout.CENTER);
        
        // Hover effect
        cardPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cardPanel.setBackground(hoverColor);
                cardPanel.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cardPanel.setBackground(cardBackground);
                cardPanel.repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                card.action.accept(null);
            }
        });
        
        return cardPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout(20, 0));
        statusBar.setOpaque(false);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 60)),
            new EmptyBorder(15, 0, 0, 0)
        ));
        
        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftStatus.setOpaque(false);
        
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 12f));
        statusLabel.setForeground(new Color(180, 180, 180));
        
        backendLabel.setFont(backendLabel.getFont().deriveFont(Font.PLAIN, 12f));
        backendLabel.setForeground(new Color(180, 180, 180));
        
        leftStatus.add(createStatusIndicator());
        leftStatus.add(statusLabel);
        leftStatus.add(createSeparator());
        leftStatus.add(backendLabel);
        
        statusBar.add(leftStatus, BorderLayout.WEST);
        
        return statusBar;
    }

    private JPanel createStatusIndicator() {
        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(76, 175, 80));
                g2d.fillOval(2, 5, 8, 8);
                
                g2d.dispose();
            }
        };
        indicator.setOpaque(false);
        indicator.setPreferredSize(new Dimension(12, 18));
        return indicator;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 18));
        sep.setForeground(new Color(70, 70, 70));
        return sep;
    }

    private JPanel makePlaceholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(43, 43, 43));
        
        JTextArea ta = new JTextArea(12, 50);
        ta.setEditable(false);
        ta.setBackground(cardBackground);
        ta.setForeground(new Color(200, 200, 200));
        ta.setFont(new Font("Monospace", Font.PLAIN, 13));
        ta.setBorder(new EmptyBorder(15, 15, 15, 15));
        ta.setText(text + " - Feature coming soon!\n\nYou can execute firewall-cmd commands using the CommandRunner utility.");
        
        JScrollPane sp = new JScrollPane(ta);
        sp.setBorder(null);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void openWindow(String title, JPanel panel) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(900, 600);
        f.setLocationRelativeTo(this);
        f.add(panel);
        f.setVisible(true);
    }

    private void onReload(ActionEvent e) {
        JTextArea tmp = new JTextArea();
        new Thread(() -> CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--reload"), tmp, true)).start();
        showOutputDialog("Reload Firewall", tmp);
    }

    private void onShowStatus(ActionEvent e) {
        JTextArea tmp = new JTextArea();
        new Thread(() -> CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--state"), tmp, false)).start();
        showOutputDialog("Firewall State", tmp);
    }

    private void onShowZones(ActionEvent e) {
        JTextArea tmp = new JTextArea();
        new Thread(() -> CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--get-active-zones"), tmp, false)).start();
        showOutputDialog("Active Zones", tmp);
    }

    private void showOutputDialog(String title, JTextArea area) {
        area.setEditable(false);
        area.setFont(new Font("Monospace", Font.PLAIN, 13));
        area.setBackground(cardBackground);
        area.setForeground(new Color(220, 220, 220));
        area.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(700, 400));
        sp.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 70)));
        
        JDialog dialog = new JDialog(this, title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setBackground(new Color(43, 43, 43));
        dialog.add(sp);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showCard(String name) {
        if (centerCards == null) return;
        SwingUtilities.invokeLater(() -> {
            CardLayout cl = (CardLayout) centerCards.getLayout();
            cl.show(centerCards, name);
        });
    }

    private void checkStatus() {
        JTextArea s1 = new JTextArea();
        new Thread(() -> {
            CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--state"), s1, false);
            JTextArea s2 = new JTextArea();
            CommandRunner.runCommand(Arrays.asList("firewall-cmd", "--info-backend"), s2, false);
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Status: " + s1.getText().trim().replaceAll("\n", " "));
                backendLabel.setText("Backend: " + s2.getText().trim().replaceAll("\n", " "));
            });
        }).start();
    }

    private static class FeatureCard {
        String title;
        String description;
        String icon;
        java.util.function.Consumer<ActionEvent> action;

        FeatureCard(String title, String description, String icon, java.util.function.Consumer<ActionEvent> action) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.action = action;
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            
            Color accentColor = new Color(82, 145, 255);
            
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.track", new Color(43, 43, 43));
            UIManager.put("ScrollPane.smoothScrolling", true);
            
            UIManager.put("Button.default.startBackground", new Color(60, 63, 65));
            UIManager.put("Button.default.endBackground", new Color(50, 53, 55));
            UIManager.put("Button.default.borderColor", new Color(85, 85, 85));
            
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.focusColor", accentColor);
            
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", true);
            UIManager.put("Table.gridColor", new Color(50, 50, 50));
            
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame f = new MainFrame();
                f.setVisible(true);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }
}