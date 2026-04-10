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

package zone.moddev.patchy.updatecheckers.forge;

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

public final class ForgeUpdateChecker extends AbstractUpdateChecker<MinecraftForgeVersions> {

    private static final String CHANGELOG_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/%s/forge-%s-changelog.txt";

    public ForgeUpdateChecker() {
        super(NotifierConfiguration.<MinecraftForgeVersions>builder()
            .name("forge")
            .type(UpdateCheckerType.FORGE)
            .serializer(new JsonSerializer<>(Constants.GSON, MinecraftForgeVersions.class))
            .versionComparator(NotifierConfiguration.notEqual())
            .webhookInfo(new WebhookInfo("Forge Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125547430096966/unknown.png"))
            .build());
    }

    @Override
    protected @NotNull MinecraftForgeVersions queryLatest() {
        return new MinecraftForgeVersions(ForgeVersionHelper.getForgeVersions());
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(@Nullable final MinecraftForgeVersions oldVersion, final MinecraftForgeVersions newVersion) {
        if (oldVersion == null) {
            final Map.Entry<String, String> versionEntry = newVersion.byMcVersion().entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .orElseThrow();

            final String mcVersion = versionEntry.getKey();
            final String version = versionEntry.getValue();

            final EmbedBuilder embed = new EmbedBuilder();
            embed.addField("Minecraft Version", mcVersion, true);
            embed.setTitle("New Forge Update Released!");
            embed.setColor(0x0000FF);
            embed.addField("Forge Version", version, true);
            addChangelog(embed, null, version);
            return List.of(embed);
        }

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
                final String currentForgeVersion = entry.getValue();
                final String oldForgeVersion = oldVersion.byMcVersion().get(mcVersion);

                final EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("New Forge Update Released!");
                embed.addField("Minecraft Version", mcVersion, true);
                embed.setColor(0x0000FF);

                if (oldForgeVersion == null) {
                    embed.addField("Version", currentForgeVersion, true);
                } else {
                    boolean isNoLongerBeta = isNoLongerBeta(oldForgeVersion, currentForgeVersion);
                    embed.addField(isNoLongerBeta ? "New stable release" : "Latest Forge Version", "**%s** -> **%s**".formatted(oldForgeVersion, currentForgeVersion), true);
                }

                addChangelog(embed, oldForgeVersion, currentForgeVersion);
                return embed;
            })
            .collect(Collectors.toList());
    }

    private static boolean isNoLongerBeta(String oldForgeVersionFull, String newForgeVersionFull) {
        try {
            final String oldForgeVersion = oldForgeVersionFull.substring(oldForgeVersionFull.indexOf('-') + 1);
            final String newForgeVersion = newForgeVersionFull.substring(newForgeVersionFull.indexOf('-') + 1);

            final String[] oldVersionParts = oldForgeVersion.split("\\.");
            final String[] newVersionParts = newForgeVersion.split("\\.");

            if (oldVersionParts.length > 1 && newVersionParts.length > 1) {
                boolean wasBeta = oldVersionParts[1].equals("0");
                boolean isNowStable = !newVersionParts[1].equals("0");
                return wasBeta && isNowStable;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable String forgeStart, String forgeEnd) {
        try {
            String changelog = getChangelogBetweenVersions(
                forgeStart, forgeEnd
            );
            if (changelog == null || changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "MinecraftForge/Forge");

            embedBuilder.setDescription(SharedVersionHelpers.truncate("""
                [Changelog](%s):
                %s
                """.formatted(
                CHANGELOG_URL.formatted(forgeEnd, forgeEnd), changelog
            ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(@Nullable final String forgeStart, final String forgeEnd) throws IOException {
        if (forgeStart == null || forgeStart.equals(forgeEnd)) {
            String content = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(forgeEnd, forgeEnd));
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

        final var startChangelog = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(forgeStart, forgeStart));
        final var endChangelog = NetworkUtils.getUrlContent(CHANGELOG_URL.formatted(forgeEnd, forgeEnd));

        if (startChangelog == null || endChangelog == null) {
            return "";
        }

        return endChangelog.replace(startChangelog, "");
    }
}
