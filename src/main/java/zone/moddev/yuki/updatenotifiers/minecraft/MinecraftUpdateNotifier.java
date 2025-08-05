package zone.moddev.yuki.updatenotifiers.minecraft;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zone.moddev.yuki.Yuki;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.updatenotifiers.UpdateNotifier;
import zone.moddev.yuki.util.StringSerializer;

import java.awt.Color;

public final class MinecraftUpdateNotifier extends UpdateNotifier<MinecraftVersionHelper.VersionsInfo> implements Runnable {

    public MinecraftUpdateNotifier() {
        super(NotifierConfiguration.<MinecraftVersionHelper.VersionsInfo>builder()
                .name("minecraft")
                .channelGetter(GuildConfiguration::getMinecraftChannel)
                .versionComparator(NotifierConfiguration.notEqual())
                .serializer(StringSerializer.json(StringSerializer.RECORD_GSON, MinecraftVersionHelper.VersionsInfo.class))
                .webhookInfo(new WebhookInfo("Minecraft Updates", "https://media.discordapp.net/attachments/957353544493719632/1005934698767323156/unknown.png?width=594&height=594"))
                .build());
        Yuki.LOGGER.warn("Attempting to check for new Minecraft versions!");
    }

    @Override
    protected MinecraftVersionHelper.VersionsInfo queryLatest() {
        final var meta = MinecraftVersionHelper.getMeta();
        if (meta == null) {
            return null;
        }
        return meta.latest;
    }

    @NotNull
    @Override
    protected EmbedBuilder getEmbed(@Nullable final MinecraftVersionHelper.VersionsInfo oldVersion, final @NotNull MinecraftVersionHelper.VersionsInfo newVersion) {
        if (oldVersion == null) {
            return new EmbedBuilder()
                    .setDescription("New Minecraft version available!")
                    .setColor(Color.CYAN)
                    .setDescription(newVersion.snapshot());
        }
        final var embed = new EmbedBuilder();
        if (!oldVersion.release().equals(newVersion.release())) {
            // https://www.minecraft.net/en-us/article/minecraft-java-edition-1-18-1
            embed.setTitle("New Minecraft release available!");
            embed.setDescription(newVersion.release() + "\nChangelog: " + "https://www.minecraft.net/en-us/article/minecraft-java-edition-%s".formatted(newVersion.release().replace('.', '-')));
            embed.setColor(Color.GREEN);
        } else {
            if (newVersion.snapshot().contains("-rc")) {
                // https://www.minecraft.net/en-us/article/minecraft-1-19-4-release-candidate-1
                embed.setTitle("New Minecraft Release Candidate available!");
                final String[] split = newVersion.snapshot().split("-");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: "
                        + "https://www.minecraft.net/en-us/article/minecraft-%s-release-candidate-%s"
                        .formatted(split[0].replace('.', '-'), split[1].substring(2)));
                embed.setColor(Color.PINK);
            } else if (newVersion.snapshot().contains("-pre")) {
                // https://www.minecraft.net/en-us/article/minecraft-1-19-4-pre-release-2
                embed.setTitle("New Minecraft Pre-Release available!");
                final String[] split = newVersion.snapshot().split("-");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: "
                        + "https://www.minecraft.net/en-us/article/minecraft-%s-pre-release-%s"
                        .formatted(split[0].replace('.', '-'), split[1].substring(3)));
                embed.setColor(Color.ORANGE);
            } else {
                // https://www.minecraft.net/en-us/article/minecraft-snapshot-23w07a
                embed.setTitle("New Minecraft snapshot available!");
                embed.setDescription(newVersion.snapshot() + "\nChangelog: " + "https://www.minecraft.net/en-us/article/minecraft-snapshot-%s".formatted(newVersion.snapshot()));
                embed.setColor(Color.CYAN);
            }
        }
        return embed;
    }
}
