package zone.moddev.yuki.updatenotifiers.fabric;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.UpdateNotifier;
import zone.moddev.yuki.util.StringSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FabricApiUpdateNotifier extends UpdateNotifier<String> {

    public FabricApiUpdateNotifier() {
        super(UpdateNotifier.NotifierConfiguration.<String>builder()
                .name("fabricapi")
                .channelGetter(GuildConfiguration::getFabricChannel)
                .serializer(StringSerializer.SELF)
                .versionComparator(NotifierConfiguration.notEqual())
                .webhookInfo(new WebhookInfo("Fabric Updates", "https://media.discordapp.net/attachments/957353544493719632/1006125360129265734/unknown.png"))
                .build());
    }

    @Nullable
    @Override
    protected String queryLatest() {
        return FabricVersionHelper.getLatestApi();
    }

    @Override
    protected @NotNull EmbedBuilder getEmbed(@Nullable final String oldVersion, @Nonnull final String newVersion) {
        return new EmbedBuilder()
                .setTitle("New Fabric API release available!")
                .setDescription(newVersion)
                .setColor(0xDBD2B5);
    }
}
