# OpenChat

![Release (latest by date)](https://img.shields.io/github/v/release/TavstalDev/OpenChat?style=plastic-square)
![Workflow Status](https://img.shields.io/github/actions/workflow/status/TavstalDev/OpenChat/ghrelease.yml?branch=stable&label=build&style=plastic-square)
![License](https://img.shields.io/github/license/TavstalDev/OpenChat?style=plastic-square)
![Downloads](https://img.shields.io/github/downloads/TavstalDev/OpenChat/total?style=plastic-square)
![Issues](https://img.shields.io/github/issues/TavstalDev/OpenChat?style=plastic-square)

## Description
**OpenChat** is a Paper plugin that moderates chat messages to prevent spam, excessive capitalization, advertisements, and inappropriate language.
It offers customizable settings for chat moderation.

## Features

- **Anti-Spam**: Detects and prevents message and command spamming with configurable thresholds.
- **Anti-Spam Whitelist**: Allows specific players or commands to bypass spam checks.
- **Anti-Advertisement**: Detect and block messages containing unallowed websites or IP addresses.
- **Anti-Caps**: Limits the percentage of capital letters in messages.
- **Anti-Swear**: Filters out inappropriate language based on a customizable list of banned words via regex patterns.
- **Command Blocking**: Block specific commands from being executed by players.
- **Tab Completion Limiter**: Restrict what commands players can see in tab completion based on permissions.
- **Customizable Chat Format**: Modify the chat message format to include player prefixes, suffixes, and other information.
- **OP Protection**: Prevent unauthorized player op status changes.
- **Private Messaging**: Send private messages between players.
- **Player Mentions**: Notify players when they are mentioned in chat.
- **Player Ignore**: Allow players to ignore messages, mentions and private messages from specific players.
- **Actions**: Configurable commands to be executed when violation is detected.
- **Anti-Swear and Anti-Advertisement Extras**: Block text containing these not just in chat but also in other places like signs, books, and anvils.

## TODO
Features planned for future releases:

- [ ] **Add Separate Logging for Violations**: Create a dedicated log file to record all chat violations for easier monitoring and detection of false positives.
- [ ] **Custom Join/Quit Messages**: Allow global and per-player customization of join and quit messages.
- [ ] **Custom Welcome Messages**: Send personalized welcome messages to players upon joining the server.
- [ ] **Player Name Hover Info**: Show additional player information when hovering over their name in chat.
- [ ] **Violation Counter and Punishments**: Implement a system to track player violations and apply punishments based on configurable thresholds.
- [ ] **Improve Anti-Spam Detection**: Enhance spam detection algorithms to reduce false positives and improve accuracy.
- [ ] **Velocity Support**: Add compatibility with Velocity proxy servers and cross-server chat messaging.
- [ ] **GUI Configuration**: Implement a graphical user interface for easier configuration of plugin settings.

Please note that this plugins' focus is on chat, player input and its moderation, so I do not plan to add such features that are related to tabs, scoreboards, or bossbars.
If you search a plugin for those features, consider using [TAB](https://www.spigotmc.org/resources/tab-1-8-x-1-21-x.57806/).

Also, I do not plan to add custom punishment features, like mute, kick or ban commands, these commands should be handled by a dedicated punishment plugin.

## Commands and Permissions

| Command                             | Permission                     | Description                                                                                    |
|-------------------------------------|--------------------------------|------------------------------------------------------------------------------------------------|
| `/openchat`                         | `openchat.commands.openchat`   | Displays plugin information and available commands.                                            |
| `/openchat help`                    | `openchat.commands.help`       | Shows help information for OpenChat commands.                                                  |
| `/openchat version`                 | `openchat.commands.version`    | Displays the current version of the OpenChat plugin.                                           |
| `/openchat reload`                  | `openchat.commands.reload`     | Reloads the plugin configuration.                                                              |
| `/openchat clear`                   | `openchat.commands.clear`      | Clears the chat for all players.                                                               |
| `/chattoggle`                       | `openchat.commands.chattoggle` | Toggles public chat visibility for the player. (requires chat radious to be set)               |
| `/msg <player> <message>`           | `openchat.commands.whisper`    | Sends a private message to another player.                                                     |
| `/msgtoggle`                        | `openchat.commands.whisper`    | Toggles receiving private messages for the player.                                             |
| `/reply <message>`                  | `openchat.commands.reply`      | Replies to the last private message received.                                                  |
| `/ignore <player>`                  | `openchat.commands.ignore`     | Ignores messages, mentions, and private messages from the specified player.                    |
| `/unignore <player>`                | `openchat.commands.unignore`   | Unignores messages, mentions, and private messages from the specified player.                  |
| `/socialspy`                        | `openchat.commands.socialspy`  | Toggles social spy mode to see private messages between other players.                         |
| `/mentions help `                   | `openchat.commands.mentions`   | Shows help information for player mentions.                                                    |
| `/mentions sound <sound>`           | `openchat.commands.mentions`   | Sets the sound played when the player is mentioned.                                            |
| `/mentions display <display>`       | `openchat.commands.mentions`   | Sets how mentions are displayed (e.g., chat, actionbar, sound).                                |
| `/mentions preference <preference>` | `openchat.commands.mentions`   | Sets the player's mention preference (e.g., always, silent in combat, never in combat, never). |

## Dependencies

To ensure the plugin functions correctly, your server must have the following plugins installed:

- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)**: Required for using placeholders in chat formats and messages.
- **[CombatLogX](https://www.spigotmc.org/resources/combatlogx.31689/)** (optional): Recommended for better integration with combat logging and to enhance mention preferences during combat.

## Contributing

I welcome contributions! If you have ideas for features, bug fixes, or improvements, please consider contributing to the project.

1.  **Fork** the repository on GitHub.
2.  **Create a new branch** for your feature or bug fix (e.g., `feature/add-category` or `fix/gui-bug`).
3.  **Commit your changes** with clear, concise, and descriptive commit messages.
4.  **Push your branch** to your forked repository.
5.  **Open a Pull Request** to the `main` branch of this repository, describing your changes.

## License

This project is licensed under the **GNU General Public License v3.0**. You can find the full license text in the `LICENSE` file within this repository.

## Contact

For any questions, bug reports, or feature requests, please use the [GitHub issue tracker](https://github.com/TavstalDev/OpenChat/issues).