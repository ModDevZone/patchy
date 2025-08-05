package zone.moddev.yuki;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.sqlite.SQLiteDataSource;
import zone.moddev.yuki.config.ConfigManager;
import zone.moddev.yuki.config.GeneralConfiguration;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.UpdateNotifiers;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;

public class Yuki {

    public static final Logger LOGGER = LoggerFactory.getLogger("Yuki Bot");
    private static final String VERSION = Yuki.class.getPackage().getImplementationVersion();
    private static final String GITHUB_REPO = "https://github.com/moddevzone/yuki";
    private static final String WEBSITE_URL = "https://moddev.zone/discord-bots/yuki";

    private static final Set<GatewayIntent> INTENTS = EnumSet.of(
            GatewayIntent.GUILD_WEBHOOKS,
            GatewayIntent.GUILD_MESSAGES);

    private static final Set<CacheFlag> DISABLED_CACHE_FLAGS = EnumSet.of(
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS,
            CacheFlag.VOICE_STATE,
            CacheFlag.ACTIVITY,
            CacheFlag.SCHEDULED_EVENTS);

    private static final Set<Permission> PERMISSIONS = EnumSet.of(
            Permission.MESSAGE_MANAGE,
            Permission.MANAGE_WEBHOOKS);

    private static Yuki instance;

    public static Yuki getInstance() {
        return instance;
    }

    public static JDA getJDA() {
        return getInstance() == null ? null : getInstance().getJda();
    }

    private static JDA jda;
    private static Jdbi jdbi;
    private ConfigManager configManager;
    private static String version;

    public static void main(String[] args) {
        try {
            instance = new Yuki();
            instance.launchBot();
        } catch (Exception exception) {
            LOGGER.error("Failed to launch Yuki Bot...", exception);
            System.exit(1);
        }
    }

    public void launchBot() {
        instance = this;
        configManager = new ConfigManager(Path.of("yuki-bot/config"));

        if (configManager.getGlobalConfig().getBotToken().isEmpty()) {
            throw new IllegalStateException("Please provide a bot token to be able to launch the bot...");
        }

        if (configManager.getGlobalConfig().getBotOwners().isEmpty()) {
            throw new IllegalStateException("Please provide at least one bot owner in the config before we can start...");
        }

        final var dbPath = Path.of("yuki-bot/database").resolve("yuki_data.db");
        if (!Files.exists(dbPath)) {
            try {
                Files.createFile(dbPath);
            } catch (IOException exception) {
                throw new RuntimeException("Exception creating database!", exception);
            }
        }

        final var url = "jdbc:sqlite:" + dbPath;
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        dataSource.setDatabaseName("YukiBot");

        final var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:yuki/db")
                .load();
        flyway.migrate();

        jdbi = Jdbi.create(dataSource);

        try {
            final var builder = JDABuilder.create(configManager.getGlobalConfig().getBotToken(), INTENTS);
            DISABLED_CACHE_FLAGS.forEach(builder::disableCache);
            jda = builder.build().awaitReady();
            LOGGER.info("Successfully connected to Discord!");
        } catch (InvalidTokenException exception) {
            LOGGER.error("Bot initialization was interrupted! Invalid token provided!", exception);
            System.exit(1);
        } catch (InterruptedException exception) {
            LOGGER.error("Bot initialization was interrupted!", exception);
            System.exit(1);
        }
        UpdateNotifiers.init();
    }

    private void shutdown() {
        jda.shutdown();
        configManager = null;
        instance = null;
    }

    public JDA getJda() {
        return jda;
    }

    public Jdbi getJdbi() {
        return jdbi;
    }

    public GuildConfiguration getGuildConfig(long guildId) {
        return configManager.getGuildConfig(guildId);
    }

    public GeneralConfiguration getGlobalConfig() {
        return configManager.getGlobalConfig();
    }
}
