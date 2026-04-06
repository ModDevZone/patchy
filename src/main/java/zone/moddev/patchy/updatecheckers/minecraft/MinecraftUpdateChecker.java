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
 * OUT of OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package zone.moddev.patchy.updatecheckers.minecraft;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.minecraft.MinecraftVersionHelper.VersionsInfo;
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.JsonSerializer;
import zone.moddev.patchy.util.NetworkUtils;

import java.util.List;

public final class MinecraftUpdateChecker extends AbstractUpdateChecker<VersionsInfo> {

    private static final String CHANGELOG_BASE_URL = "https://www.minecraft.net/en-us/article/minecraft-";

    public MinecraftUpdateChecker() {
        super(NotifierConfiguration.<MinecraftVersionHelper.VersionsInfo>builder()
            .name("minecraft")
            .type(UpdateCheckerType.MINECRAFT)
            .versionComparator(NotifierConfiguration.notEqual())
            .serializer(new JsonSerializer<>(Constants.GSON, VersionsInfo.class))
            .webhookInfo(new WebhookInfo("Minecraft Updates", "https://media.discordapp.net/attachments/957353544493719632/1005934698767323156/unknown.png?width=594&height=594"))
            .build());
    }

    @Override
    protected VersionsInfo queryLatest() {
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) {
            return null;
        }
        return meta.latest;
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(@Nullable final VersionsInfo oldVersion, final @NotNull VersionsInfo newVersion) {
        if (oldVersion == null) {
            return List.of(new EmbedBuilder()
                .setDescription("New Minecraft Version Available!")
                .setColor(0x00FFFF)
                .setDescription(newVersion.snapshot()));
        }

        VersionType versionType = getVersionType(oldVersion, newVersion);
        String version = versionType == VersionType.RELEASE ? newVersion.release() : newVersion.snapshot();
        String changelogUrl = versionType.getChangelogUrl(version);

        final EmbedBuilder embed = new EmbedBuilder()
            .setTitle(versionType.getDisplay() + " Available!")
            .setColor(versionType.getColor());

        if (NetworkUtils.isValidUrl(changelogUrl)) {
            embed.setDescription(version + "\nChangelog: " + changelogUrl);
        } else {
            embed.setDescription(version);
        }

        return List.of(embed);
    }

    private VersionType getVersionType(VersionsInfo oldVersion, VersionsInfo newVersion) {
        if (!oldVersion.release().equals(newVersion.release())) {
            return VersionType.RELEASE;
        } else if (newVersion.snapshot().contains("-release-candidate-")) {
            return VersionType.RELEASE_CANDIDATE;
        } else if (newVersion.snapshot().contains("-pre")) {
            return VersionType.PRE_RELEASE;
        } else {
            return VersionType.SNAPSHOT;
        }
    }

    private enum VersionType {
        RELEASE("New Minecraft Release", 0x00FF00, "java-edition-%s"),
        RELEASE_CANDIDATE("New Minecraft Release Candidate", 0xFFAFAF, "%s"),
        PRE_RELEASE("New Minecraft Pre-Release", 0xFFC800, "%s"),
        SNAPSHOT("New Minecraft Snapshot", 0x00ffff, "%s");

        private final String display;
        private final int color;
        private final String urlPath;

        VersionType(String display, int color, String urlPath) {
            this.display = display;
            this.color = color;
            this.urlPath = urlPath;
        }

        public String getDisplay() {
            return display;
        }

        public int getColor() {
            return color;
        }

        public String getChangelogUrl(String version) {
            return CHANGELOG_BASE_URL + String.format(urlPath, version.replace('.', '-'));
        }
    }
}
