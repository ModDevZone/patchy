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

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.fabric.FabricVersionHelper;
import zone.moddev.patchy.util.SelfSerializer;

import java.util.List;

public final class FabricLoaderUpdateChecker extends AbstractUpdateChecker<String> {

    public FabricLoaderUpdateChecker() {
        super(NotifierConfiguration.<String>builder()
                .name("fabricloader")
                .type(UpdateCheckerType.FABRIC)
                .serializer(new SelfSerializer())
                .versionComparator(NotifierConfiguration.notEqual())
                .webhookInfo(new WebhookInfo("Fabric Loader Updates", "https://github.com/fabricmc.png"))
                .build());
    }

    @Nullable
    @Override
    protected String queryLatest() {
        return FabricVersionHelper.getLatestFabricLoaderVersion();
    }

    @Override
    protected @NotNull List<EmbedBuilder> getEmbeds(@Nullable final String oldVersion, final @NotNull String newVersion) {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("New Fabric Loader Update Released!");
        embed.setColor(0xDBD2B5);

        if (oldVersion == null) {
            embed.addField("Fabric Loader Version", newVersion, true);
        } else {
            embed.addField("Latest Fabric Loader Version", "**%s** -> **%s**".formatted(oldVersion, newVersion), true);
        }
        return List.of(embed);
    }
}
