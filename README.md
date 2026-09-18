# Patchy

A basic announcement bot for posting updates about modding libraries, tools, and Minecraft to Discord.

## Features

- **Minecraft:** Java edition updates.
- **Modding Tools:** Blockbench updates.
- **Mod Loaders:** NeoForge, Forge, and Fabric updates.
- **Mappings:** Parchment mappings updates.

## Adding Patchy to Your Server

To add the official public instance of Patchy to your Discord server, **[use this invite link](https://discord.com/oauth2/authorize?client_id=1290086547017957406&permissions=536947712&scope=bot+applications.commands)**.

You must have the **Manage Server** permission to add the bot. (You can also self-host this bot, see #self-hosting below)

## Setup and Configuration

Once the bot is in your server, you need to configure it.

### 1. Set the Controller Role (Optional)

By default, only users with the **Manage Server** permission can change the bot's settings. If you want to allow other users to manage the bot, you can assign a specific role.

-   `/patchy-config set-role role:<role-name>`
    -   **Permission:** `Manage Server`
    -   **Example:** `/patchy-config set-role role:@BotManager`

### 2. Configure Notification Channels

Tell the bot where to send update notifications.

-   `/patchy-config set type:<update-type> channel:<#channel-name>`
    -   **Permission:** `Manage Server` or Controller Role
    -   Sets the notification channel for a specific update type.
    -   **Example:** `/patchy-config set type:minecraft channel:#mc-updates`

-   `/patchy-config unset type:<update-type>`
    -   **Permission:** `Manage Server` or Controller Role
    -   Disables notifications for a specific update type.
    -   **Example:** `/patchy-config unset type:forge`

-   `/patchy-config view`
    -   **Permission:** `Manage Server` or Controller Role
    -   Shows the current notification channel settings for all update types.

## Commands

| Command           | Subcommand | Description                                     | Permissions Required               |
| ----------------- | ---------- |-------------------------------------------------| ---------------------------        |
| `/version`        |            | Shows the bot's current version and info.       | Everyone                           |
| `/shutdown`       |            | Shuts down the bot safely.                      | Bot Owner Only (KiriCattus)        |
| `/patchy-config`  | `set`      | Sets a notification channel for an update type. | `Manage Server` or Controller Role |
|                   | `unset`    | Disables notifications for an update type.      | `Manage Server` or Controller Role |
|                   | `view`     | Views the current notification settings.        | `Manage Server` or Controller Role |
|                   | `set-role` | Sets the role that can manage the bot.          | `Manage Server`                    |

## Self-Hosting

### 1. Inviting to your server
It is recommended to set up the bot in its own dedicated folder. This will help contain the bot's executable, along with the folders and files it generates.

-   Go to the Discord Developer Portal and create a new bot application.
-   Navigate to the "OAuth2" -> "URL Generator" section.
-   Select the following scopes:
    -   `bot`
    -   `applications.commands`
-   Under "Bot Permissions", select the following:
    -   `View Channels` (to be able to view the channels ready to send update notifications in)
    -   `Send Messages` (to be able to send messages in the channels you set)
    -   `Manage Messages` (for publishing in announcement channels)
    -   `Manage Webhooks` (so the bot can use custom names and images for each update type)
    -   `Read Message History` (no we don't care about message history, it's to help commands work correctly)
-   Copy and use the generated URL to invite the bot to your server(s).
-   Go to the "Bot" tab and copy your bot's **token**.

### 2. Launching the bot

It is recommended to run the bot in its own dedicated folder.

-   To run this bot, you will need **Java 25** or newer.
-   When launching the bot, you must include the `--enable-native-access=ALL-UNNAMED` argument to prevent a warning error from one of the bot's dependencies.
    **Example:** `java --enable-native-access=ALL-UNNAMED -jar patchy-<VERSION>-all.jar`

### 3. Configuration

On the first run, the bot will generate a `patchy-bot-configs` directory. Inside, you will find `patchy-config.json`. You must edit this file and provide your bot's token and your user ID for the owner-only commands.

```json
{
  "botOwnerId": "<THE_BOT_OWNERS_ID>",
  "discordToken": "<THE_BOTS_TOKEN>"
}
```

Once configured you should be able to launch the bot!

## Support

For questions or support, please visit the official [Mod Dev Zone Discord server](https://discord.moddev.zone) or open an issue on the [GitHub repository](https://github.com/moddevzone/patchy).
