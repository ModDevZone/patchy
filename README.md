# Patchy

### A basic announcement bot for posting updates about modding libraries, tools and Minecraft to Discord, this includes the following:
- Minecraft
- Blockbench
- NeoForge
- Forge
- Fabric
- Parchment

## Setup and Launch

It is recommended to set up the bot in its own dedicated folder. This will help contain the bot's executable, along with the folders and files it generates.

- `patchy-bot`: Contains the core bot configuration and per-guild configuration files.  
- - `bot-logs`: Contains console output logs. These logs are automatically pruned and will not exceed 2GB in size or last more than 30 days.
- - `guild-configs`: Per guild configuration files, when the bot is added to a new server a file will generate and need filling out for posts to be sent to Discord.  

To run this bot, you will need to use **Java 25**.

When launching the bot, you must include the `--enable-native-access=ALL-UNNAMED` argument to prevent an error warning from one of the bot's dependencies.

**Example launch command:**
```sh
java --enable-native-access=ALL-UNNAMED -jar patchy-all.jar
```

### Configuration
On the first run, the bot will fail to launch and shut down. After this, you will see a few new folders with blank configuration files in them configuration files.  
You will need to fill out these configuration files for the bot to launch and post updates correctly.
