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
