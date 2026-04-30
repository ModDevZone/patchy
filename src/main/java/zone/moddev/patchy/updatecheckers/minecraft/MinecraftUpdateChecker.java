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

package zone.moddev.patchy.updatecheckers.minecraft;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.util.NetworkUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class MinecraftUpdateChecker extends AbstractUpdateChecker<MinecraftUpdateChecker.MinecraftVersion> {

    private static final String CHANGELOG_BASE_URL = "https://www.minecraft.net/en-us/article/minecraft-";

    private static final String KEY_RELEASE = "release";
    private static final String KEY_SNAPSHOT = "snapshot";

    public MinecraftUpdateChecker() {
        super(MinecraftUpdateChecker.MinecraftVersion.class, NotifierConfiguration.<MinecraftUpdateChecker.MinecraftVersion>builder(UpdateCheckerType.MINECRAFT)
                .versionComparator(NotifierConfiguration.notEqual())
                .versionKeyExtractor(MinecraftVersion::version)
                .webhookInfo(new WebhookInfo("Minecraft Updates", "https://media.discordapp.net/attachments/957353544493719632/1005934698767323156/unknown.png?width=594&height=594"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() {
        return List.of(KEY_RELEASE, KEY_SNAPSHOT);
    }

    @Override
    protected Map<String, MinecraftUpdateChecker.MinecraftVersion> fetchLatest() throws IOException {
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) {
            return Map.of();
        }

        var release = new MinecraftUpdateChecker.MinecraftVersion(meta.latest.release(), VersionType.RELEASE);
        var snapshot = new MinecraftUpdateChecker.MinecraftVersion(meta.latest.snapshot(), getSnapshotVersionType(meta.latest.snapshot()));

        return Map.of(KEY_RELEASE, release, KEY_SNAPSHOT, snapshot);
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(String key, @Nullable final MinecraftUpdateChecker.MinecraftVersion oldVersion, final @NotNull MinecraftUpdateChecker.MinecraftVersion newVersion) {
        if (oldVersion == null) {
            return List.of(new EmbedBuilder()
                    .setDescription("New Minecraft Version Available!")
                    .setColor(0x00FFFF)
                    .setDescription(newVersion.version()));
        }

        String changelogUrl = newVersion.type.getChangelogUrl(newVersion.version());

        final EmbedBuilder embed = new EmbedBuilder()
                .setTitle(newVersion.type.getDisplay() + " Available!")
                .setColor(newVersion.type.getColor());

        if (NetworkUtils.isValidUrl(changelogUrl)) {
            embed.setDescription(newVersion.version() + "\nChangelog: " + changelogUrl);
        } else {
            embed.setDescription(newVersion.version());
        }

        return List.of(embed);
    }

    private VersionType getSnapshotVersionType(String version) {
        if (version.contains("-rc")) {
            return VersionType.RELEASE_CANDIDATE;
        } else if (version.contains("-pre")) {
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
            String formattedVersion = version.replace('.', '-');
            if (this == RELEASE_CANDIDATE) {
                formattedVersion = formattedVersion.replace("-rc", "-release-candidate");
            }
            return CHANGELOG_BASE_URL + String.format(urlPath, formattedVersion);
        }

        public boolean isRelease() {
            return this == RELEASE;
        }
    }

    protected record MinecraftVersion(String version, VersionType type) {}
}
