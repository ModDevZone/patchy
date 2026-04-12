/*
 * MIT License
 *
 * Copyright (c) 2016 - 2026 Mod Dev Zone
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package zone.moddev.patchy;

import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.moddev.patchy.commands.ConfigCommand;
import zone.moddev.patchy.commands.ShutdownCommand;
import zone.moddev.patchy.commands.VersionCommand;
import zone.moddev.patchy.configs.ConfigManager;
import zone.moddev.patchy.configs.GuildConfigListener;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.blockbench.BlockbenchUpdateChecker;
import zone.moddev.patchy.updatecheckers.fabric.api.FabricApiUpdateChecker;
import zone.moddev.patchy.updatecheckers.fabric.loader.FabricLoaderUpdateChecker;
import zone.moddev.patchy.updatecheckers.forge.ForgeUpdateChecker;
import zone.moddev.patchy.updatecheckers.minecraft.MinecraftUpdateChecker;
import zone.moddev.patchy.updatecheckers.neoforge.NeoForgeUpdateChecker;
import zone.moddev.patchy.updatecheckers.parchment.ParchmentUpdateChecker;
import zone.moddev.patchy.util.dao.UpdateCheckerDAO;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Patchy {

    public static final Logger LOGGER = LoggerFactory.getLogger("Patchy");
    public static final String GITHUB_REPO = "https://github.com/moddevzone/patchy";
    public static final String WEBSITE_URL = "https://moddev.zone/bots/patchy";

    private final JDA jda;
    private final Jdbi jdbi;
    private final ConfigManager configManager;
    private static Patchy instance;
    private final HikariDataSource ds;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final Set<GatewayIntent> INTENTS = EnumSet.of(
            GatewayIntent.GUILD_WEBHOOKS,
            GatewayIntent.GUILD_MESSAGES);

    private static final Set<CacheFlag> DISABLED_CACHE_FLAGS = EnumSet.of(
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.VOICE_STATE,
            CacheFlag.ACTIVITY,
            CacheFlag.SCHEDULED_EVENTS,
            CacheFlag.EMOJI,
            CacheFlag.STICKER);

    public Patchy() {
        instance = this;
        configManager = new ConfigManager();

        try {
            JDABuilder builder = JDABuilder.createDefault(configManager.getPatchyConfig().getApiKey(), INTENTS);
            DISABLED_CACHE_FLAGS.forEach(builder::disableCache);
            builder.addEventListeners(new GuildConfigListener(configManager), new ShutdownCommand(), new ConfigCommand(), new VersionCommand());

            jda = builder.build().awaitReady();

            SubcommandData setCommand = new SubcommandData("set", "Set a notification channel for an update type.")
                    .addOptions(
                            new OptionData(OptionType.STRING, "type", "The type of updates to notify for.", true)
                                    .addChoices(Arrays.stream(UpdateCheckerType.values())
                                            .map(e -> new Command.Choice(e.name().toLowerCase(), e.name().toLowerCase()))
                                            .collect(Collectors.toList())),
                            new OptionData(OptionType.CHANNEL, "channel", "The channel to send notifications to.", true)
                    );

            SubcommandData unsetCommand = new SubcommandData("unset", "Disable notifications for an update type.")
                    .addOptions(
                            new OptionData(OptionType.STRING, "type", "The type of updates to disable notifications for.", true)
                                    .addChoices(Arrays.stream(UpdateCheckerType.values())
                                            .map(e -> new Command.Choice(e.name().toLowerCase(), e.name().toLowerCase()))
                                            .collect(Collectors.toList()))
                    );

            SubcommandData setRoleCommand = new SubcommandData("set-role", "Set the role that can manage the bot's configuration.")
                    .addOption(OptionType.ROLE, "role", "The role to grant permission to edit this bot's configuration.", true);

            jda.updateCommands().addCommands(
                    Commands.slash("shutdown", "Shuts down the bot."),
                    Commands.slash("version", "Shows the current running version of the bot."),
                    Commands.slash("patchy-config", "Configure update notification channels.")
                            .addSubcommands(setCommand, unsetCommand, new SubcommandData("view", "View the currently set update notification channels."), setRoleCommand)
            ).queue();

            LOGGER.info("Successfully connected to Discord!");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(exception);
        }

        ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:sqlite:data.db");
        jdbi = Jdbi.create(ds);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.useExtension(UpdateCheckerDAO.class, UpdateCheckerDAO::createTable);

        String VERSION = Patchy.class.getPackage().getImplementationVersion();
        LOGGER.info("""
                          \n
                          _______    ______  ________   ______   __    __  __      __\s
                         |       \\  /      \\|        \\ /      \\ |  \\  |  \\|  \\    /  \\
                         | $$$$$$$\\|  $$$$$$\\\\$$$$$$$$|  $$$$$$\\| $$  | $$ \\$$\\  /  $$
                         | $$__/ $$| $$__| $$  | $$   | $$   \\$$| $$__| $$  \\$$\\/  $$\s
                         | $$    $$| $$    $$  | $$   | $$      | $$    $$   \\$$  $$ \s
                         | $$$$$$$ | $$$$$$$$  | $$   | $$   __ | $$$$$$$$    \\$$$$  \s
                         | $$      | $$  | $$  | $$   | $$__/  \\| $$  | $$    | $$   \s
                         | $$      | $$  | $$  | $$    \\$$    $$| $$  | $$    | $$   \s
                          \\$$       \\$$   \\$$   \\$$     \\$$$$$$  \\$$   \\$$     \\$$   \s
                          Version: {}
                          Website: {}
                          Github Source: {}
                        """,
                VERSION == null ? "DEVELOPMENT_BUILD" : VERSION, WEBSITE_URL, GITHUB_REPO);

        scheduleUpdateCheckers();
    }

    static void main() {
        try {
            new Patchy();
        } catch (Exception exception) {
            LOGGER.error("Patchy initialization failed!", exception);
            System.exit(1);
        }
    }

    public void shutdown() {
        LOGGER.info("Shutting down...");
        scheduler.shutdownNow();
        jda.shutdown();
        ds.close();
        LOGGER.info("Goodbye!");
        System.exit(0);
    }

    public static JDA getJDA() {
        return instance.jda;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static Patchy getInstance() {
        return instance;
    }

    private void scheduleUpdateCheckers() {
        LOGGER.info("Checking for Minecraft, Forge, NeoForge, and Fabric updates every 15 minutes.");
        scheduler.scheduleAtFixedRate(new MinecraftUpdateChecker(), 0, 15, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new NeoForgeUpdateChecker(), 0, 15, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new ForgeUpdateChecker(), 0, 15, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new FabricApiUpdateChecker(), 0, 15, TimeUnit.MINUTES);
        scheduler.scheduleAtFixedRate(new FabricLoaderUpdateChecker(), 0, 15, TimeUnit.MINUTES);

        LOGGER.info("Checking for Parchment and Blockbench updates every hour.");
        scheduler.scheduleAtFixedRate(new ParchmentUpdateChecker(), 0, 1, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(new BlockbenchUpdateChecker(), 0, 1, TimeUnit.HOURS);
    }
}
