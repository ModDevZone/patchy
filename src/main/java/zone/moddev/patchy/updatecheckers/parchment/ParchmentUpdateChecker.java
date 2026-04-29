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

package zone.moddev.patchy.updatecheckers.parchment;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.patchy.updatecheckers.AbstractUpdateChecker;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;
import zone.moddev.patchy.updatecheckers.parchment.ParchmentVersionHelper.ParchmentVersion;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ParchmentUpdateChecker extends AbstractUpdateChecker<ParchmentVersion> {

    public ParchmentUpdateChecker() {
        super(ParchmentVersion.class, NotifierConfiguration.<ParchmentVersion>builder(UpdateCheckerType.PARCHMENT)
                .versionComparator(Comparator.comparing(ParchmentVersion::timestamp))
                .versionKeyExtractor(ParchmentVersion::parchmentVersion)
                .webhookInfo(new WebhookInfo("Parchment Updates", "https://github.com/parchmentmc.png"))
                .build());
    }

    @Override
    protected List<String> getUpdateKeys() throws IOException {
        return ParchmentVersionHelper.getVersionKeys();
    }

    @Override
    protected Map<String, ParchmentVersion> fetchLatest() throws IOException {
        return ParchmentVersionHelper.latestByMcRelease();
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(String key, @Nullable final ParchmentVersion oldVersion, final @NotNull ParchmentVersion newVersion) {
        return List.of(new EmbedBuilder()
                .setColor(0xFF0000)
                .setTitle("New %s Parchment Version is Available!".formatted(newVersion.mcVersion()))
                .addField("Version", newVersion.parchmentVersion(), false)
                .addField("Coordinate", "`org.parchmentmc.data:parchment-%s:%s`".formatted(newVersion.mcVersion(), newVersion.parchmentVersion()), false));
    }
}
