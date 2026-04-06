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

package zone.moddev.patchy.updatecheckers.blockbench;

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
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockbenchUpdateChecker extends AbstractUpdateChecker<GithubRelease> {

    public BlockbenchUpdateChecker() {
        super(NotifierConfiguration.<GithubRelease>builder()
                .name("blockbench")
                .type(UpdateCheckerType.BLOCKBENCH)
                .versionComparator(Comparator.comparing(release -> Instant.parse(release.published_at())))
                .serializer(new JsonSerializer<>(Constants.GSON, GithubRelease.class))
                .webhookInfo(new WebhookInfo("Blockbench Updates", "https://www.blockbench.net/favicon.png"))
                .build());
    }

    @Nullable
    @Override
    protected GithubRelease queryLatest() throws IOException {
        return BlockbenchVersionHelper.getLatest(loggingMarker);
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(@Nullable final GithubRelease oldVersion, final @NotNull GithubRelease newVersion) {
        final EmbedBuilder embed = new EmbedBuilder()
                .setTitle("New Blockbench %s: %s".formatted(newVersion.prerelease() ? "pre-release" : "release", newVersion.name()))
                .setColor(newVersion.prerelease() ? 0x29CFD8 : 0x1E93D9)
                .setDescription(SharedVersionHelpers.truncate(Stream.of(newVersion.body().split("\n"))
                        .map(str -> str.trim().startsWith("#") ? "**" + str.replace("#", "") + "**" : str)
                        .collect(Collectors.joining("\n")), MessageEmbed.DESCRIPTION_MAX_LENGTH / 2));

        if (NetworkUtils.isValidUrl(newVersion.html_url())) {
            embed.setUrl(newVersion.html_url());
        }

        return List.of(embed);
    }
}
