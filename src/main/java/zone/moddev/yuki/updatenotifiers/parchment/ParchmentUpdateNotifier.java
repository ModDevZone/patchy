package zone.moddev.yuki.updatenotifiers.parchment;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.UpdateNotifier;
import zone.moddev.yuki.util.StringSerializer;

import java.awt.Color;
import java.util.Comparator;

public final class ParchmentUpdateNotifier extends UpdateNotifier<ParchmentVersionHelper.ParchmentVersion> {

    public ParchmentUpdateNotifier() {
        super(NotifierConfiguration.<ParchmentVersionHelper.ParchmentVersion>builder()
                .name("parchment")
                .channelGetter(GuildConfiguration::getParchmentChannel)
                .versionComparator(Comparator.comparing(ParchmentVersionHelper.ParchmentVersion::getDate))
                .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, ParchmentVersionHelper.ParchmentVersion.class))
                .webhookInfo(new WebhookInfo("Parchment Updates", "https://media.discordapp.net/attachments/957353544493719632/1006189498960466010/unknown.png"))
                .build());
    }

    @Nullable
    @Override
    protected ParchmentVersionHelper.ParchmentVersion queryLatest() {
        return ParchmentVersionHelper.newest(ParchmentVersionHelper.byMcReleases());
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final ParchmentVersionHelper.ParchmentVersion oldVersion, final @NotNull ParchmentVersionHelper.ParchmentVersion newVersion) {
        return new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("A new %s Parchment version is available!".formatted(newVersion.mcVersion()))
                .addField("Version", newVersion.parchmentVersion(), false)
                .addField("Coordinate", "`org.parchmentmc.data:parchment-%s:%s`".formatted(newVersion.mcVersion(), newVersion.parchmentVersion()), false);
    }
}
