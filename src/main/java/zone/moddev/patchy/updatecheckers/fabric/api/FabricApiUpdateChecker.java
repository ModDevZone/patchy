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

package zone.moddev.patchy.updatecheckers.fabric.api;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.JsonSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class FabricApiUpdateChecker extends AbstractUpdateChecker<FabricApiVersions> {

    public FabricApiUpdateChecker() {
        super(NotifierConfiguration.<FabricApiVersions>builder()
                .name("fabricapi")
                .type(UpdateCheckerType.FABRIC)
                .serializer(new JsonSerializer<>(Constants.GSON, FabricApiVersions.class))
                .versionComparator(NotifierConfiguration.notEqual())
                .webhookInfo(new WebhookInfo("Fabric API Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Override
    protected FabricApiVersions queryLatest() {
        return new FabricApiVersions(FabricVersionHelper.getFabricApiVersions());
    }

    @Override
    protected @NotNull List<EmbedBuilder> getEmbeds(@Nullable final FabricApiVersions oldVersion, final @NotNull FabricApiVersions newVersion) {
        if (oldVersion == null) {
            // First run, just announce the latest version available.
            final Map.Entry<String, String> versionEntry = newVersion.byMcVersion().entrySet().stream()
                    .max(Map.Entry.comparingByKey())
                    .orElseThrow();

            final String mcVersion = versionEntry.getKey();
            final String version = versionEntry.getValue();

            final EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("New Fabric API Update Released!");
            embed.setColor(0xDBD2B5);
            embed.addField("Minecraft Version", mcVersion, true);
            embed.addField("Fabric API Version", version, true);
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
                    final String currentApiVersion = entry.getValue();
                    final String oldApiVersion = oldVersion.byMcVersion().get(mcVersion);

                    final EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("New Fabric API Update Released!");
                    embed.setColor(0xDBD2B5);
                    embed.addField("Minecraft Version", mcVersion, true);

                    if (oldApiVersion == null) {
                        embed.addField("Fabric API Version", currentApiVersion, true);
                    } else {
                        embed.addField("Latest Fabric API Version", "**%s** -> **%s**".formatted(oldApiVersion, currentApiVersion), true);
                    }
                    return embed;
                })
                .collect(Collectors.toList());
    }
}
