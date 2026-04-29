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

import com.unascribed.flexver.FlexVerComparator;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class FabricApiUpdateChecker extends AbstractUpdateChecker<FabricApiVersion> {

    public FabricApiUpdateChecker() {
        super(FabricApiVersion.class, NotifierConfiguration.<FabricApiVersion>builder(UpdateCheckerType.FABRIC_API)
                .versionComparator((o1, o2) -> FlexVerComparator.compare(o1.apiPart(), o2.apiPart()))
                .versionKeyExtractor(FabricApiVersion::apiPart)
                .webhookInfo(new WebhookInfo("Fabric API Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() throws IOException {
        return FabricVersionHelper.getFabricApiVersions().keySet().stream().toList();
    }

    @Override
    protected Map<String, FabricApiVersion> fetchLatest() throws IOException {
        return FabricVersionHelper.getFabricApiVersions();
    }

    @Override
    protected @NotNull List<EmbedBuilder> getEmbeds(String mcVersion, @Nullable final FabricApiVersion oldVersion, final @NotNull FabricApiVersion newVersion) {
        // First run, just announce the latest version available.
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New Fabric API Update Released!");
        embed.setColor(0xDBD2B5);
        embed.addField("Minecraft Version", mcVersion, true);
        if (oldVersion == null) {
            embed.addField("Fabric API Version", newVersion.fullVersion(), true);
        } else {
            embed.addField("Latest Fabric API Version", "**%s** -> **%s**".formatted(oldVersion.fullVersion(), newVersion.fullVersion()), true);
        }
        return List.of(embed);
    }
}
