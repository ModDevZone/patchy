package zone.moddev.patchy.updatecheckers.neoforge;

import com.unascribed.flexver.FlexVerComparator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.util.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NeoForgeUpdateChecker extends AbstractUpdateChecker<NeoForgeVersion> {

    public static final String CHANGELOG_URL_TEMPLATE = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-changelog.txt";

    public NeoForgeUpdateChecker() {
        super(NeoForgeVersion.class, NotifierConfiguration.<NeoForgeVersion>builder(UpdateCheckerType.NEOFORGE)
                .versionComparator((oldVersion, newVersion) -> FlexVerComparator.compare(oldVersion.id(), newVersion.id()))
                .versionKeyExtractor(NeoForgeVersion::id)
                .webhookInfo(new WebhookInfo("NeoForge Updates", "https://github.com/NeoForged.png"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() throws IOException {
        return NeoForgeVersionHelper.getNeoForgeVersions().keySet().stream().toList();
    }

    @Override
    protected Map<String, NeoForgeVersion> fetchLatest() throws IOException {
        return NeoForgeVersionHelper.getNeoForgeVersions();
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(String mcVersion, @Nullable final NeoForgeVersion oldVersion, @NotNull final NeoForgeVersion newVersion) throws IOException {
        var list = new ArrayList<EmbedBuilder>();

        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New NeoForge Update Released!");
        embed.setColor(0xFFFF00);
        embed.addField("Minecraft Version", mcVersion, true);

        if (oldVersion == null) {
            embed.addField("NeoForge Version", newVersion.id(), true);
        } else {
            embed.addField("Latest NeoForge Version", "**%s** -> **%s**".formatted(oldVersion, newVersion), true);
        }

        addChangelog(embed, oldVersion, newVersion);
        list.add(embed);

        var isNoLongerBeta = oldVersion != null && oldVersion.id().endsWith("-beta") && !newVersion.id().endsWith("-beta");
        if(isNoLongerBeta) {
            list.add(new EmbedBuilder().setTitle("NeoForge for Minecraft %s is now stable!".formatted(mcVersion)).setColor(0x57F287));
        }

        return list;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable NeoForgeVersion neoStart, NeoForgeVersion neoEnd) throws IOException {
        String changelog = getChangelogBetweenVersions(neoStart, neoEnd);
        if (changelog == null || changelog.isBlank()) return;

        changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "NeoForged/NeoForge");

        embedBuilder.setDescription(SharedVersionHelpers.truncate("""
                    [Changelog](%s):
                    %s
                    """.formatted(
                CHANGELOG_URL_TEMPLATE.formatted(neoEnd, neoEnd), changelog
        ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
    }

    public static String getChangelogBetweenVersions(@Nullable final NeoForgeVersion neoStart, final NeoForgeVersion neoEnd) throws IOException {
        String content = NetworkUtils.getUrlContent(CHANGELOG_URL_TEMPLATE.formatted(neoEnd, neoEnd));
        if (content == null) return "";

        if (neoStart == null || neoStart.equals(neoEnd)) {
            final String[] split = content.split("\n");
            final StringBuilder changelog = new StringBuilder(split[0])
                    .append('\n');
            for (int i = 1; i < split.length; i++) {
                if (split[i].startsWith(" - ")) break;
                changelog.append(split[i]).append('\n');
            }
            return changelog.toString();
        }

        var startMarker = " - `%s`".formatted(neoStart);
        return content.substring(0, content.indexOf(startMarker)).trim();
    }
}
