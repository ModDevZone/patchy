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
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.JsonSerializer;

import java.util.Comparator;
import java.util.List;

public final class ParchmentUpdateChecker extends AbstractUpdateChecker<ParchmentVersion> {

    public ParchmentUpdateChecker() {
        super(NotifierConfiguration.<ParchmentVersion>builder()
            .name("parchment")
            .type(UpdateCheckerType.PARCHMENT)
            .versionComparator(Comparator.comparing(ParchmentVersion::getDate))
            .serializer(new JsonSerializer<>(Constants.GSON, ParchmentVersion.class))
            .webhookInfo(new WebhookInfo("Parchment Updates", "https://media.discordapp.net/attachments/957353544493719632/1006189498960466010/unknown.png"))
            .build());
    }

    @Nullable
    @Override
    protected ParchmentVersion queryLatest() {
        return ParchmentVersionHelper.newest(ParchmentVersionHelper.byMcReleases());
    }

    @NotNull
    @Override
    protected List<EmbedBuilder> getEmbeds(@Nullable final ParchmentVersion oldVersion, final @NotNull ParchmentVersion newVersion) {
        return List.of(new EmbedBuilder()
            .setColor(0xFF0000)
            .setTitle("A new %s Parchment version is available!".formatted(newVersion.mcVersion()))
            .addField("Version", newVersion.parchmentVersion(), false)
            .addField("Coordinate", "`org.parchmentmc.data:parchment-%s:%s`".formatted(newVersion.mcVersion(), newVersion.parchmentVersion()), false));
    }
}
