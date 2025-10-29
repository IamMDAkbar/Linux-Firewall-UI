# Linux Firewall - Kali Firewall Manager (Swing)

This project provides a Java Swing UI for managing `firewalld` on Kali Linux. It uses FlatLaf for a modern dark theme and runs `firewall-cmd` via `pkexec` / `ProcessBuilder`.

Important files:
- `src/main/java/com/firewall/MainFrame.java` - main dashboard (home page)
- `src/main/java/com/firewall/CommandRunner.java` - utility to run commands and stream output to a JTextArea
- `src/main/java/com/firewall/PortManagerPanel.java` - UI to add/remove/list ports
- `src/main/java/com/firewall/ZoneManagerPanel.java` - basic zone management UI
- `src/main/java/com/firewall/ServicesPanel.java` - services listing and add/remove
- `src/main/java/com/firewall/RichRulesPanel.java` - builder UI for firewalld "rich rules" (create/list/remove)
- `src/main/java/com/firewall/PortForwardingPanel.java` - UI for managing port forwarding rules
- `src/main/java/com/firewall/MasqueradingPanel.java` - controls for zone masquerading (SNAT)
- `src/main/java/com/firewall/ICMPControlPanel.java` - manage ICMP message blocking
- `src/main/java/com/firewall/IPSetsPanel.java` - create and manage IP sets
- `src/main/java/com/firewall/LoggingMonitoringPanel.java` - configure logging and monitor firewall activity

How to build:
```bash
cd /home/kali/FIREWALL_UI
mvn clean package
# Kali Firewall Manager (Swing)

A lightweight Java Swing desktop UI for managing firewalld on Linux (developed on Kali). The app provides convenient panels for zones, services, ports, runtime actions and a dashboard with quick status checks. It uses FlatLaf for a modern theme and executes firewall commands via the `CommandRunner` utility.

Repository
-- Source: this repository (local copy)
-- Live fork/credits: https://github.com/IamMDAkbar/LinuxFirewallUI

Prerequisites
- Linux distribution with firewalld (firewall-cmd) installed (tested on Kali Linux).
- Java 11 (OpenJDK 11) or later installed.
- Maven 3.x to build from source (or use the included shaded JAR if present).
- A graphical session (X11/Wayland) to show the Swing UI.

Optional (for privilege-less commands)
- sudo/visudo access to add a NOPASSWD rule for `/usr/bin/firewall-cmd`, or configure PolicyKit/pkexec as desired. See the `Settings` -> `Privilege setup` helper in the app.

Quick start (build & run)
1. From the project root:

```bash
./run.sh
# or build manually
mvn -DskipTests=true package
java -jar target/firewall-ui-1.0-SNAPSHOT-jar-with-dependencies.jar
```

2. Open the application and use the left sidebar to navigate: Home, Dashboard, Settings, Documentation, Quick Actions.

Folder structure (important files)
- `src/main/java/com/firewall/`
	- `MainFrame.java` — application window, sidebar, content area and CardLayout for panels.
	- `CommandRunner.java` — runs external commands (firewall-cmd) and streams output into JTextArea.
	- `DashboardPanel.java` — shows firewall state, counts and a refresh action.
	- `SettingsPanel.java` — theme choice, background chooser, and privilege setup helper (Preferences-backed).
	- `DocumentationPanel.java` — loads `docs.md` and displays help text.
	- `PortManagerPanel.java`, `ZoneManagerPanel.java`, `ServicesPanel.java` — feature panels wired from MainFrame.
	- `PortManagerPanel.java`, `ZoneManagerPanel.java`, `ServicesPanel.java`, `PortForwardingPanel.java` — feature panels wired from MainFrame.
	- `PortManagerPanel.java`, `ZoneManagerPanel.java`, `ServicesPanel.java`, `PortForwardingPanel.java`, `MasqueradingPanel.java`, `ICMPControlPanel.java`, `IPSetsPanel.java` — feature panels wired from MainFrame.
- `src/main/resources/docs.md` — documentation loaded into the app.
- `run.sh` — convenience script to build+run via Maven exec.
- `pom.xml` — build manifest (Maven dependencies).

Code flow (high level)
1. `MainFrame` is the app entry UI. It sets up the theme and initializes panels using a CardLayout (`home`, `dashboard`, `settings`, `docs`).
2. User actions (sidebar or cards) trigger small UI handlers which either open new windows (`openWindow`) or switch cards.
3. `CommandRunner.runCommand(...)` executes `firewall-cmd` (optionally prefixed by `pkexec`) using `ProcessBuilder`. It streams output into a `JTextArea` on the EDT.
4. `DashboardPanel` and feature panels call `CommandRunner` on background threads then update UI components on the Swing EDT.
5. `SettingsPanel` uses `java.util.prefs.Preferences` to persist simple settings (theme, background path, enable flag for Claude Sonnet 3.5 placeholder).

Features
- Dashboard: status, counts for zones/services/ports and a Refresh action.
- Quick Actions: reload firewall, show state, show active zones.
- Feature cards: Zone Management, Services Control, Port Management, and placeholders for advanced features.
 - Rich Rules: a simple rule-builder that constructs firewalld rich rule strings, previews them, and calls `firewall-cmd --add-rich-rule` / `--remove-rich-rule` or `--list-rich-rules` for a zone.
- Settings: theme switching (Light/Dark), background chooser, privilege helper (visudo line copy/test).
- Documentation: in-app docs pulled from `src/main/resources/docs.md`.

Security & Privileges
- The app executes privileged commands. Preferred secure approaches:
	- Use `visudo` to allow a limited NOPASSWD rule for `/usr/bin/firewall-cmd` (recommended over running the whole UI as root).
	- Create a small wrapper script that restricts allowed arguments and permit only that wrapper in sudoers if you need stricter control.

Credits
- Original/fork reference: https://github.com/IamMDAkbar/Linux-Firewall-UI

License
- This project currently does not include an explicit license file. Add a LICENSE if you intend to open-source it.

