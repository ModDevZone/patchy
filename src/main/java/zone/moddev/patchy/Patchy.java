package zone.moddev.patchy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zone.moddev.patchy.configs.ConfigManager;
import zone.moddev.patchy.configs.GuildConfig;
import zone.moddev.patchy.configs.GuildConfigListener;
import zone.moddev.patchy.configs.PatchyConfig;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

public class Patchy {

    public static final Logger LOGGER = LoggerFactory.getLogger("Patchy Bot");
    private static final String VERSION = Patchy.class.getPackage().getImplementationVersion();
    private static final String GITHUB_REPO = "https://github.com/moddevzone/patchy";
    private static final String WEBSITE_URL = "https://moddev.zone/bots/patchy";

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

    private static final Set<Permission> PERMISSIONS = EnumSet.of(
            Permission.MESSAGE_MANAGE,
            Permission.MANAGE_WEBHOOKS);

    private static Patchy instance;

    private final JDA jda;
    private final ConfigManager configManager;
    private final PatchyConfig patchyConfig;

    public Patchy() throws IOException {
        instance = this;
        this.configManager = new ConfigManager();
        this.patchyConfig = configManager.loadPatchyConfig();
        this.jda = initJDA();
    }

    public static void main(String[] args) {
        try {
            new Patchy();
        } catch (Exception e) {
            LOGGER.error("Failed to launch Patchy Bot...", e);
            System.exit(1);
        }
    }
    
    private JDA initJDA() {
        try {
            JDABuilder builder = JDABuilder.createDefault(patchyConfig.getApiKey(), INTENTS);
            DISABLED_CACHE_FLAGS.forEach(builder::disableCache);

            builder.addEventListeners(new GuildConfigListener(configManager));

            JDA jda = builder.build().awaitReady();
            LOGGER.info("Successfully connected to Discord!");
            return jda;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public static Patchy getInstance() {
        return instance;
    }

    public JDA getJda() {
        return jda;
    }

    public PatchyConfig getConfig() {
        return configManager.loadPatchyConfig();
    }

    public GuildConfig getGuildConfig(String guildId) {
        return configManager.loadOrCreateGuildConfig(guildId);
    }

    public void saveGuildConfig(String guildId, GuildConfig config) {
        configManager.saveGuildConfig(guildId, config);
    }
}
