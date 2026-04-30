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
import java.util.*;

public final class ForgeUpdateChecker extends AbstractUpdateChecker<ForgeVersion> {

    private static final String CHANGELOG_URL_TEMPLATE = "https://maven.minecraftforge.net/net/minecraftforge/forge/%s/forge-%s-changelog.txt";

    public ForgeUpdateChecker() {
        super(ForgeVersion.class, NotifierConfiguration.<ForgeVersion>builder(UpdateCheckerType.FORGE)
                .versionComparator((o1, o2) -> FlexVerComparator.compare(o1.id(), o2.id()))
                .versionKeyExtractor(ForgeVersion::id)
                .webhookInfo(new WebhookInfo("Forge Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125547430096966/unknown.png"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() throws IOException {
        return ForgeVersionHelper.getForgeVersions().keySet().stream().toList();
    }

    @Override
    protected Map<String, ForgeVersion> fetchLatest() throws IOException {
        return ForgeVersionHelper.getForgeVersions();
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(String mcVersion, @Nullable final ForgeVersion oldVersion, final ForgeVersion newVersion) {
        var list = new ArrayList<EmbedBuilder>();

        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New MinecraftForge Update Released!");
        embed.setColor(0x0000FF);
        embed.addField("Minecraft Version", mcVersion, true);

        if (oldVersion == null) {
            embed.addField("Forge Version", newVersion.id(), true);
        } else {
            embed.addField("Latest Forge Version", "**%s** -> **%s**".formatted(oldVersion, newVersion), true);
        }

        addChangelog(embed, oldVersion, newVersion);
        list.add(embed);

        var isNoLongerBeta = oldVersion != null && isNoLongerBeta(oldVersion.id(), newVersion.id());
        if(isNoLongerBeta) {
            list.add(new EmbedBuilder().setTitle("Forge for Minecraft %s is now stable!".formatted(mcVersion)).setColor(0x57F287));
        }

        return list;
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

    private static void addChangelog(EmbedBuilder embedBuilder, @Nullable ForgeVersion forgeStart, ForgeVersion forgeEnd) {
        try {
            String changelog = getChangelogBetweenVersions(forgeStart, forgeEnd);
            if (changelog == null || changelog.isBlank()) return;

            changelog = SharedVersionHelpers.replaceGitHubReferences(changelog, "MinecraftForge/Forge");

            embedBuilder.setDescription(SharedVersionHelpers.truncate("""
                    [Changelog](%s):
                    %s
                    """.formatted(
                    CHANGELOG_URL_TEMPLATE.formatted(forgeEnd, forgeEnd), changelog
            ), MessageEmbed.DESCRIPTION_MAX_LENGTH));
        } catch (IOException ignored) {
        }
    }

    public static String getChangelogBetweenVersions(@Nullable final ForgeVersion forgeStart, final ForgeVersion forgeEnd) throws IOException {
        String content = NetworkUtils.getUrlContent(CHANGELOG_URL_TEMPLATE.formatted(forgeEnd, forgeEnd));
        if (content == null) return "";

        if (forgeStart == null || forgeStart.equals(forgeEnd)) {
            final String[] split = content.split("\n");
            final StringBuilder changelog = new StringBuilder(split[0])
                    .append('\n');
            for (int i = 1; i < split.length; i++) {
                if (split[i].startsWith(" - ")) break;
                changelog.append(split[i]).append('\n');
            }
            return changelog.toString();
        }

        var startMarker = " - %s".formatted(forgeStart);
        return content.substring(0, content.indexOf(startMarker)).trim();
    }
}
