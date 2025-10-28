# Kali Firewall Manager (Swing)

This project provides a Java Swing UI for managing `firewalld` on Kali Linux. It uses FlatLaf for a modern dark theme and runs `firewall-cmd` via `pkexec` / `ProcessBuilder`.

Important files:
- `src/main/java/com/firewall/MainFrame.java` - main dashboard (home page)
- `src/main/java/com/firewall/CommandRunner.java` - utility to run commands and stream output to a JTextArea
- `src/main/java/com/firewall/PortManagerPanel.java` - UI to add/remove/list ports
- `src/main/java/com/firewall/ZoneManagerPanel.java` - basic zone management UI
- `src/main/java/com/firewall/ServicesPanel.java` - services listing and add/remove

How to build:
```bash
cd /home/kali/FIREWALL_UI
mvn clean package
```

Run (graphical environment required):
```bash
java -jar target/firewall-ui-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Notes:
- This application executes system commands and may require `pkexec` or sudo privileges. Running from a headless session without X11 will fail when attempting to open UI windows.
- The UI is modular. Add or extend feature panels under `com.firewall` as needed.
