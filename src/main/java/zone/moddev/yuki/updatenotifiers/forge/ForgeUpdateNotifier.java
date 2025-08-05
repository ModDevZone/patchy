package zone.moddev.yuki.updatenotifiers.forge;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.SharedVersionHelpers;
import zone.moddev.yuki.updatenotifiers.UpdateNotifier;
import zone.moddev.yuki.util.StringSerializer;
import zone.moddev.yuki.util.Utils;

import java.awt.Color;
import java.io.IOException;
import java.net.URI;

public final class ForgeUpdateNotifier extends UpdateNotifier<MinecraftForgeVersion> {

    private static final String CHANGELOG_URL_TEMPLATE
            = "https://maven.minecraftforge.net/net/minecraftforge/forge/%1$s-%2$s/forge-%1$s-%2$s-changelog.txt";

    public ForgeUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftForgeVersion>builder()
                .name("forge")
                .channelGetter(GuildConfiguration::getForgeChannel)
                .versionComparator(NotifierConfiguration.notEqual())
                .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, MinecraftForgeVersion.class))
                .webhookInfo(new WebhookInfo("Forge Updates", "https://github.com/MinecraftForge.png"))
                .build());
    }

    @Override
    protected @NotNull MinecraftForgeVersion queryLatest() throws IOException {
        return ForgeVersionHelper.getLatestMcVersionForgeVersions();
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final MinecraftForgeVersion oldVersion, final @NotNull MinecraftForgeVersion newVersion) {
        final var embed = new EmbedBuilder();
        embed.addField("Minecraft Version", newVersion.mcVersion(), true);
        embed.setTitle("Forge version update");
        embed.setColor(Color.ORANGE);

        final var mcVersion = newVersion.mcVersion();
        final var latest = newVersion.forgeVersion();

        if (oldVersion == null || !oldVersion.mcVersion().equals(newVersion.mcVersion())) {
            embed.addField("Version", latest.getLatest(), true);
            addChangelog(embed, mcVersion, latest.getLatest(), mcVersion, latest.getLatest());
            return embed;
        }

        final var lastForgeVersions = oldVersion.forgeVersion();
        if (latest.getLatest() != null && !lastForgeVersions.getLatest().equals(latest.getLatest())) {
            final var start = lastForgeVersions.getLatest();
            final var end = latest.getLatest();
            embed.addField("Latest", String.format("**%s** -> **%s**%n", start, end), true);
            addChangelog(embed, mcVersion, start, mcVersion, end);
        }

        if (latest.getRecommended() != null) {
            if (lastForgeVersions.getRecommended() == null) {
                final var version = latest.getRecommended();
                embed.addField("Recommended", String.format("*none* -> **%s**%n", version),
                        true);
                embed.setDescription(MarkdownUtil.maskedLink("Changelog", String.format(CHANGELOG_URL_TEMPLATE,
                        mcVersion, latest.getRecommended())));
            } else if (!latest.getRecommended().equals(lastForgeVersions.getRecommended())) {
                final var start = lastForgeVersions.getRecommended();
                final var end = latest.getRecommended();
                embed.addField("Recommended", String.format("**%s** -> **%s**%n", start, end), true);
                addChangelog(embed, mcVersion, start, mcVersion, end);
            }
        }
        return embed;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, String mcStart, String forgeStart, String mcEnd, String forgeEnd) {
        try {
            String changelog = getChangelogBetweenVersions(
                    mcStart, forgeStart, mcEnd, forgeEnd
            );
            if (changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "MinecraftForge/MinecraftForge");

            embedBuilder.setDescription("""
                    [Changelog](%s):
                    %s
                    """.formatted(
                    CHANGELOG_URL_TEMPLATE.formatted(mcEnd, forgeEnd), changelog
            ));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(final String startMc, final String startForge, final String endMc, final String endForge) throws IOException {
        final var startUrl = URI.create(CHANGELOG_URL_TEMPLATE.formatted(startMc, startForge)).toURL();
        final var endUrl = URI.create(CHANGELOG_URL_TEMPLATE.formatted(endMc, endForge)).toURL();

        final var startMcVersionSplit = startMc.split("\\.");
        final var startForgeVersionSplit = startForge.split("\\.");
        final var startChangelog = Utils.getUrlAsString(startUrl).replace("""
                %s.%s.x Changelog
                %s.%s
                ====""".formatted(startMcVersionSplit[0], startMcVersionSplit[1], startForgeVersionSplit[0], startForgeVersionSplit[1]), "");

        final var endChangelog = Utils.getUrlAsString(endUrl);
        var changelog = endChangelog.replace(startChangelog, "");

        final var endMcVersionSplit = endMc.split("\\.");
        final var endForgeVersionSplit = endForge.split("\\.");
        changelog = changelog.replace("""
                %s.%s.x Changelog
                %s.%s
                ====""".formatted(endMcVersionSplit[0], endMcVersionSplit[1], endForgeVersionSplit[0], endForgeVersionSplit[1]), "");

        if (changelog.startsWith("\n")) {
            changelog = changelog.substring(1);
        }

        return changelog;
    }

}
