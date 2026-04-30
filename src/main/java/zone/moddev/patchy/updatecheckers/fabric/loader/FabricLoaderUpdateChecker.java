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

package zone.moddev.patchy.updatecheckers.fabric.loader;

import com.unascribed.flexver.FlexVerComparator;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;

import java.util.List;

public final class FabricLoaderUpdateChecker extends AbstractUpdateChecker.Single<FabricLoaderVersion> {

    // TODO config?
    private static final boolean INCLUDE_BETA_VERSIONS = true;

    public FabricLoaderUpdateChecker() {
        super(FabricLoaderVersion.class, NotifierConfiguration.<FabricLoaderVersion>builder(UpdateCheckerType.FABRIC_LOADER)
                .versionComparator((o1, o2) -> FlexVerComparator.compare(o1.version(), o2.version()))
                .versionKeyExtractor(FabricLoaderVersion::version)
                .webhookInfo(new WebhookInfo("Fabric Loader Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Nullable
    @Override
    protected FabricLoaderVersion fetchLatestSingle() {
        return FabricVersionHelper.getLatestLoaderVersion(INCLUDE_BETA_VERSIONS).orElseGet(() -> {
            Patchy.LOGGER.error(loggingMarker, "Unable to retrieve latest fabric loader version!");
            return null;
        });
    }

    @Override
    protected List<EmbedBuilder> getEmbedsSingle(@Nullable final FabricLoaderVersion oldVersion, final @NotNull FabricLoaderVersion newVersion) {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(newVersion.stable() ? "New Fabric Loader Update Released!" : "New Fabric Loader Beta Released!");
        embed.setColor(newVersion.stable() ? 0xDBD2B5 : 0xDBC3B5);

        if (oldVersion == null) {
            embed.addField("Fabric Loader Version", newVersion.version(), true);
        } else {
            embed.addField("Latest Fabric Loader Version", "**%s** -> **%s**".formatted(oldVersion.version(), newVersion.version()), true);
        }
        return List.of(embed);
    }
}
