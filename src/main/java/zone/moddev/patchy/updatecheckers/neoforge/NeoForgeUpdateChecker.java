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

package zone.moddev.patchy.updatecheckers.neoforge;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.SharedVersionHelpers;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.JsonSerializer;
import zone.moddev.patchy.util.NetworkUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class NeoForgeUpdateChecker extends AbstractUpdateChecker<NeoForgeVersions> {

    public static final String CHANGELOG_URL = "https://maven.neoforged.net/releases/net/neoforged/neoforge/%s/neoforge-%s-changelog.txt";

    public NeoForgeUpdateChecker() {
        super(NotifierConfiguration.<NeoForgeVersions>builder()
                .name("neoforge")
                .type(UpdateCheckerType.NEOFORGE)
                .serializer(new JsonSerializer<>(Constants.GSON, NeoForgeVersions.class))
                .versionComparator(NotifierConfiguration.notEqual())
                .webhookInfo(new WebhookInfo("NeoForge Updates", "https://github.com/NeoForged.png"))
                .build());
    }

    @Override
    protected NeoForgeVersions queryLatest() {
        return new NeoForgeVersions(NeoForgeVersionHelper.getNeoForgeVersions());
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(@Nullable final NeoForgeVersions oldVersion, @NotNull final NeoForgeVersions newVersion) {
        if (oldVersion == null) {
            // First run, just announce the latest version available.
            final Map.Entry<String, String> versionEntry = newVersion.byMcVersion().entrySet().stream()
                    .max(Map.Entry.comparingByKey())
                    .orElseThrow();

            final String mcVersion = versionEntry.getKey();
            final String version = versionEntry.getValue();

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New NeoForge Update Released!");
            embed.setColor(0xFFFF00);
            embed.addField("Minecraft Version", mcVersion, true);
            embed.addField("NeoForge Version", version, true);
            addChangelog(embed, null, version);
            return List.of(embed);
        }

        // Find all entries that have changed.
        final List<Map.Entry<String, String>> changedEntries = newVersion.byMcVersion().entrySet().stream()
                .filter(entry -> !Objects.equals(oldVersion.byMcVersion().get(entry.getKey()), entry.getValue()))
                .sorted(Map.Entry.<String, String>comparingByKey().reversed())
                .toList();

        if (changedEntries.isEmpty()) {
            return Collections.emptyList();
        }

        return changedEntries.stream()
                .map(entry -> {
                    final String mcVersion = entry.getKey();
                    final String currentNeoVersion = entry.getValue();
                    final String oldNeoVersion = oldVersion.byMcVersion().get(mcVersion);

                    final EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("New NeoForge Update Released!");
                    embed.setColor(0xFFFF00);
                    embed.addField("Minecraft Version", mcVersion, true);

                    if (oldNeoVersion == null) {
                        embed.addField("NeoForge Version", currentNeoVersion, true);
                    } else {
                        final boolean isNoLongerBeta = oldNeoVersion.endsWith("-beta") && !currentNeoVersion.endsWith("-beta");
                        embed.addField(isNoLongerBeta ? "New Stable Release" : "Latest NeoForge Version", "**%s** -> **%s**".formatted(oldNeoVersion, currentNeoVersion), true);
                    }

                    addChangelog(embed, oldNeoVersion, currentNeoVersion);
                    return embed;
                })
                .collect(Collectors.toList());
    }

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable String neoStart, String neoEnd) {
        try {
            String changelog = getChangelogBetweenVersions(
                    neoStart, neoEnd
            );
            if (changelog == null || changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "NeoForged/NeoForge");

            embedBuilder.setDescription(SharedVersionHelpers.truncate("""
                    [Changelog](%s):
                    %s
                    """.formatted(
                    CHANGELOG_URL.formatted(neoEnd, neoEnd), changelog
            ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(@Nullable final String neoStart, final String neoEnd) throws IOException {
        if (neoStart == null || neoStart.equals(neoEnd)) {
            String content = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(neoEnd, neoEnd));
            if (content == null) return "";
            final String[] split = content.split("\n");
            final StringBuilder changelog = new StringBuilder(split[0])
                    .append('\n');
            for (int i = 1; i < split.length; i++) {
                if (split[i].startsWith(" - ")) break;
                changelog.append(split[i]).append('\n');
            }
            return changelog.toString();
        }

        final var startChangelog = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(neoStart, neoStart));
        final var endChangelog = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(neoEnd, neoEnd));

        if (startChangelog == null || endChangelog == null) {
            return "";
        }

        return endChangelog.replace(startChangelog, "");
    }
}
