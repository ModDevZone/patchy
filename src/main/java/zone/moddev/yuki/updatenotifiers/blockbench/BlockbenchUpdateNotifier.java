package zone.moddev.yuki.updatenotifiers.blockbench;

import io.github.matyrobbrt.curseforgeapi.annotation.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.UpdateNotifier;
import zone.moddev.yuki.util.StringSerializer;
import zone.moddev.yuki.util.Utils;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockbenchUpdateNotifier extends UpdateNotifier<GithubRelease> {

    public BlockbenchUpdateNotifier() {
        super(NotifierConfiguration.<GithubRelease>builder()
                .name("blockbench")
                .channelGetter(GuildConfiguration::getBlockbenchChannel)
                .versionComparator(Comparator.comparing(release -> Instant.parse(release.published_at())))
                .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, GithubRelease.class))
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
    protected EmbedBuilder getEmbed(@Nullable final GithubRelease oldVersion, final @NotNull GithubRelease newVersion) {
        return new EmbedBuilder()
                .setTitle("New Blockbench %s: %s".formatted(newVersion.prerelease() ? "pre-release" : "release", newVersion.name()), newVersion.html_url())
                .setColor(newVersion.prerelease() ? 0x29CFD8 : 0x1E93D9)
                .setDescription(Utils.truncate(Stream.of(newVersion.body().split("\n"))
                        .map(str -> str.trim().startsWith("#") ? "**" + str.replace("#", "") + "**" : str)
                        .collect(Collectors.joining("\n")), MessageEmbed.DESCRIPTION_MAX_LENGTH / 2)); // 4k char embed is big...
    }
}
