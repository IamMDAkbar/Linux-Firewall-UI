# Firewall UI Documentation

This documentation is embedded into the application and also lives at `src/main/resources/docs.md` so it can be edited without recompiling the Java sources.

## Quick usage

- Launch the helper script from the project root:

```bash
./run.sh
```

- Or build and run manually:

```bash
mvn -DskipTests=true package
java -jar target/firewall-ui-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Prerequisites & dependencies

- Linux with `firewalld` installed (provides `firewall-cmd`).
- Java 11+ (OpenJDK) and Maven 3.x for building from source.
- A graphical desktop session (X11/Wayland) for Swing UI.

Optional tools:
- `pkexec` or sudo configured for limited `firewall-cmd` usage (see Privilege setup below).

## Privilege (sudo) setup for firewall-cmd

To avoid password prompts when the app runs firewall commands, add a tightly scoped sudoers entry using `visudo`:

```
%firewall-admins ALL=(root) NOPASSWD: /usr/bin/firewall-cmd
```

Or for a single user:

```
youruser ALL=(root) NOPASSWD: /usr/bin/firewall-cmd
```

Notes:
- Keep the rule as strict as possible. Prefer allowing only `/usr/bin/firewall-cmd` (or a small wrapper script) rather than broad sudo access.

## Settings panel

- Settings -> Privilege setup: test sudo behaviour and copy the exact visudo line for your system.
- Theme: switch between Dark and Light (applies immediately for top-level UI components).
- Background: choose an image path; saving will persist the path via `java.util.prefs.Preferences`.

## Folder structure (overview)

- `src/main/java/com/firewall/` — main sources and UI panels
- `src/main/resources/docs.md` — documentation (this file)
- `run.sh` — helper script that builds and runs the app via Maven exec
- `pom.xml` — Maven configuration and dependencies

## Code flow (short)

1. `MainFrame` initializes the UI and registers panels into a CardLayout.
2. Panels call `CommandRunner.runCommand(...)` to run `firewall-cmd` and stream output into UI components.
3. `SettingsPanel` persists user choices with Preferences and provides a helper to configure sudoers.

## Features

- Dashboard: quick status, counts and refresh.
- Quick Actions: reload firewall, show state, show active zones.
- Zone/Service/Port management panels (some are placeholders for future features).
- Settings: theme, background, and privilege helper.
- Documentation: in-app display from `docs.md`.

## Troubleshooting

- If `firewall-cmd` is missing or `firewalld` is not running, most dashboard/command features will be empty or report errors. Install `firewalld` or run the app on a machine with it available.
- If UI windows fail to open, ensure you run within a graphical session and that the DISPLAY/Wayland session is accessible.

## Credits

- This project is based on and credited to: https://github.com/IamMDAkbar/LinuxFirewallUI
# Firewall UI Documentation

## Usage

- Launch: use the included `run.sh` to build and run the app (from project root).

  ./run.sh

## Run script details

- The `run.sh` helper builds with Maven (skips tests) and launches the main class `com.firewall.MainFrame`.

## Privilege (sudo) setup for firewall-cmd

- To avoid password prompts when executing `firewall-cmd`, add a `visudo` line (edit with `sudo visudo`):

    %firewall-admins ALL=(root) NOPASSWD: /usr/bin/firewall-cmd

- Alternatively add a single user:

    youruser ALL=(root) NOPASSWD: /usr/bin/firewall-cmd

## Settings panel

- Go to Settings -> Privilege setup to test sudo configuration and copy the visudo line.
- You can choose Light/Dark theme and pick a background image.

## Notes and troubleshooting

- The Dashboard runs `firewall-cmd` commands; if `firewalld` is not available the output may be empty.
- Do not run the GUI as root unless necessary; running GUI apps as root can cause X/Wayland permission issues.
