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

### Rich Rules

The application includes a "Rich Rules" panel to build, preview, add, remove and list firewalld rich rules.

How to use:

- Open Home -> Advanced Rules -> Rich Rules.
- Select a zone and optionally choose family (ipv4/ipv6), source/destination addresses, protocol and port. Choose the action (accept/reject/drop).
- Use Preview to see the rule text in firewalld's rich rule syntax. Click "Add Rich Rule" or "Remove Rich Rule" to apply the action.

Example rich rule produced by the UI (firewall-cmd friendly):

```
rule family="ipv4" source address="1.2.3.0/24" port port="22" protocol="tcp" accept
```

The panel runs `firewall-cmd` using the same `CommandRunner` utility as other panels. Use the Settings -> Privilege setup helper to configure sudo/pkexec so the UI can run `firewall-cmd` without interactive password prompts if desired.

### Backend Configuration

The "Backend" panel allows you to manage the firewall backend used by firewalld. You can switch between:

- nftables (default, recommended): Modern firewall backend with improved performance and features
- iptables: Legacy backend for compatibility with older systems

How to use:

- Navigate to the Backend panel from the sidebar
- View current backend and configuration
- Select the desired backend from the dropdown
- Check "Make changes permanent" if you want the change to persist across reboots
- Click "Apply Backend Change" to switch backends

Important notes:
- Changing the backend may temporarily disrupt network connectivity
- Some firewall rules might need adjustment after switching backends
- It's recommended to review and test your firewall configuration after changing backends
- The change requires a firewall reload to take effect

### Lockdown Mode

The "Lockdown" panel provides a secure way to manage firewalld's lockdown feature, which prevents unauthorized applications from making changes to the firewall configuration.

How to use:

- Navigate to the Lockdown panel from the sidebar
- Use the toggle button to enable/disable lockdown mode
- When enabled, only applications explicitly whitelisted can modify firewall settings
- Use the "Allowed Services" section to manage which commands are whitelisted
- Check "Make changes permanent" to persist changes across firewall reloads

Lockdown mode is particularly useful for:
- Preventing unauthorized applications from modifying firewall rules
- Ensuring only trusted system tools can change firewall configuration
- Adding an extra layer of security to your firewall management

Note: Be careful when enabling lockdown mode as it may prevent legitimate applications from functioning properly if they need to modify firewall rules. Always ensure necessary services are whitelisted before enabling lockdown mode.

### Port Forwarding

The "Port Forwarding" panel lets you create, remove and list port forwarding (DNAT-style) rules using firewalld's forward-port feature.

How to use:

- Open Home -> Advanced Rules -> Port Forwarding.
- Choose a zone, protocol, the source port (from) and destination port (to). Optionally provide a destination address (toaddr) for DNATing to a different host.
- Use "Add Forwarding" to call `firewall-cmd --add-forward-port=port=FROM:proto=PROTO:toport=TO[:toaddr=ADDR] --zone=ZONE [--permanent]`.
- Use "Remove Forwarding" with the same spec to remove the rule. Use "List Forward Ports (zone)" to list configured forward ports for a zone.

Example add command created by the UI (conceptual):

```
firewall-cmd --zone=public --add-forward-port=port="80":proto="tcp":toport="8080"
```

If you need persistent rules, enable the "Permanent" checkbox (the UI will add `--permanent`). After adding permanent rules, reload the firewall to apply them: `firewall-cmd --reload`.

### Masquerading & NAT

The "Masquerading & NAT" panel provides simple controls for enabling or disabling masquerading (SNAT) for a specific zone. Masquerading is commonly used for NAT when a network's hosts share a single public IP.

How to use:

- Open Home -> Advanced Rules -> Masquerading & NAT.
- Choose the zone to apply masquerading to and optionally check "Permanent" to make the change persistent.
- Click "Enable Masquerade" to add masquerading for the selected zone: `firewall-cmd --zone=ZONE --add-masquerade [--permanent]`.
- Click "Disable Masquerade" to remove masquerading: `firewall-cmd --zone=ZONE --remove-masquerade [--permanent]`.
- Click "Query Masquerade" to check whether masquerading is currently enabled for the chosen zone: `firewall-cmd --zone=ZONE --query-masquerade`.

Notes:

- After adding permanent masquerade rules, run `firewall-cmd --reload` to apply them to the running configuration.
- For advanced NAT rules (complex SNAT/DNAT), consider using the Port Forwarding panel, Rich Rules, or direct `firewall-cmd --direct` iptables-level commands. The UI keeps things simple and focuses on common workflows.

### ICMP Control

The "ICMP Control" panel gives quick controls to block or unblock specific ICMP types per zone. This can be used to control ping (echo-request) or other ICMP message types for a particular zone.

How to use:

- Open Home -> Advanced Rules -> ICMP Control.
- Choose a zone and either select a common ICMP type from the dropdown or type a custom ICMP type name.
- Click "Block ICMP Type" to add the ICMP block to the zone: `firewall-cmd --zone=ZONE --add-icmp-block=TYPE [--permanent]`.
- Click "Unblock ICMP Type" to remove it: `firewall-cmd --zone=ZONE --remove-icmp-block=TYPE [--permanent]`.
- Click "List ICMP Blocks (zone)" to list blocked types for a zone: `firewall-cmd --zone=ZONE --list-icmp-blocks`.
- Click "Query ICMP Block" to check whether a particular type is blocked: `firewall-cmd --zone=ZONE --query-icmp-block=TYPE`.

Example:

```
firewall-cmd --zone=public --add-icmp-block=echo-request
```

Remember to reload the firewall after adding permanent blocks: `firewall-cmd --reload`.

### IP Sets

The "IP Sets" panel allows you to create and manage ipsets via `firewall-cmd` and manage entries in ipsets.

How to use:

- Open Home -> System & Monitoring -> IP Sets.
- To list ipsets: click "List IPSets" which runs `firewall-cmd --get-ipsets`.
- To create a new ipset: provide a name, pick a type (e.g. `hash:ip`) and family (inet/inet6), check "Permanent" if you want a permanent ipset, then click "Create IPSet". This issues:

```
firewall-cmd [--permanent] --new-ipset=NAME --type=TYPE [--family=inet|inet6]
```

- To delete an ipset: enter its name and click "Delete IPSet" (uses `--delete-ipset`).
- To view ipset info: enter the name and click "Info IPSet" (uses `--info-ipset=NAME`).
- To add/remove/list/query entries in an ipset use the entry textbox and the respective buttons. Commands used:

```
firewall-cmd [--permanent] --ipset=NAME --add-entry=ENTRY
firewall-cmd [--permanent] --ipset=NAME --remove-entry=ENTRY
firewall-cmd [--permanent] --ipset=NAME --get-entries
firewall-cmd [--permanent] --ipset=NAME --query-entry=ENTRY
```

- To list supported ipset types: click "Get IPSet Types" which runs `firewall-cmd --get-ipset-types`.

Notes:

- Creating an ipset with `--permanent` requires `firewall-cmd --reload` to apply to runtime.
- Some ipset types support options like `timeout`, `hashsize`, and `maxelem`; to use options, run `--new-ipset-from-file` or extend the UI to support additional `--option` flags.

### Logging & Monitoring

The "Logging & Monitoring" panel provides controls for managing firewall logging and real-time monitoring of firewall activity.

How to use:

- Open Home -> System & Monitoring -> Logging & Monitoring.
- Choose a zone from the dropdown and select a logging level:
  - `off`: Disable logging
  - `emerg`: Emergency messages only
  - `alert`: Alert messages
  - `crit`: Critical messages
  - `error`: Error messages
  - `warning`: Warning messages
  - `notice`: Normal but significant messages
  - `info`: Informational messages
  - `debug`: Debug-level messages

Controls available:

1. Set Logging:
   - Select zone, level, and optionally check "Permanent"
   - Click "Set Logging" to run: `firewall-cmd --zone=ZONE --set-log-denied=LEVEL [--permanent]`

2. Query Logging:
   - Shows current logging level for selected zone
   - Uses: `firewall-cmd --zone=ZONE --get-log-denied`

3. View Recent Logs:
   - Shows last 5 minutes of firewalld logs
   - Uses: `journalctl -u firewalld --since '5 minutes ago'`

4. Monitor Logs:
   - Click "Start Monitoring" to watch logs in real-time
   - Updates every 5 seconds with new firewall events
   - Click "Stop Monitoring" to end real-time monitoring

Notes:
- Setting permanent logging requires a firewall reload to apply: `firewall-cmd --reload`
- Log monitoring uses `journalctl` to read from systemd journal
- Higher log levels (like debug) may generate more verbose output
- Consider log rotation and storage capacity when enabling detailed logging

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
