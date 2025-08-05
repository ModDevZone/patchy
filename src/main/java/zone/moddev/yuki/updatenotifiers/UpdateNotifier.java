package zone.moddev.yuki.updatenotifiers;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import lombok.Builder;
import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import zone.moddev.yuki.Yuki;
import zone.moddev.yuki.config.GuildConfiguration;
import zone.moddev.yuki.config.SnowflakeValue;
import zone.moddev.yuki.util.StringSerializer;
import zone.moddev.yuki.util.dao.UpdateNotifiersDAO;
import zone.moddev.yuki.util.webhook.WebhookManager;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public abstract class UpdateNotifier<T> implements Runnable {

    public static final String WEBHOOK_NAME = "UpdateNotifiers";
    protected static final WebhookManager WEBHOOKS = WebhookManager.of(s -> s.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    protected final NotifierConfiguration<T> configuration;
    protected final Marker loggingMarker;
    private T latest;
    private boolean pickedUpFromDB;

    protected UpdateNotifier(final NotifierConfiguration<T> configuration) {
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.name);
    }

    /**
     * Queries the latest version of the listened project. <br> <br>
     * This method will be called every time this notifier {@link #run() runs},
     * so you can use it for updating versions stored externally as well.
     *
     * @return the latest version, or if one was not found, {@code null}
     * @throws IOException if an exception occurred querying the version
     */
    @Nullable
    protected abstract T queryLatest() throws IOException;

    /**
     * Gets the embed that will be sent to the channels this notifier is configured to inform.
     *
     * @param oldVersion the old version, or {@code null} if one has not been previously found
     * @param newVersion the newest found version
     * @return the embed, as a {@link EmbedBuilder builder}
     */
    @Nonnull
    protected abstract EmbedBuilder getEmbed(@Nullable T oldVersion, T newVersion);

    /**
     * Runs this notifier.
     */
    @Override
    public final void run() {
        if (Yuki.getInstance() == null) {
            Yuki.LOGGER.warn(loggingMarker, "Cannot start {} update notifier due to the bot instance being null.", configuration.name);
            return;
        }

        if (!pickedUpFromDB) {
            final String oldData = Yuki.getInstance().getJdbi()
                    .withExtension(UpdateNotifiersDAO.class, db -> db.getLatest(configuration.name));
            final Runnable initialQuery = () -> {
                try {
                    final T queried = queryLatest();
                    if (queried != null) {
                        update(queried);
                    }
                } catch (IOException exception) {
                    Yuki.LOGGER.error(loggingMarker, "An exception occurred trying to resolve latest version: ", exception);
                }
            };
            if (oldData != null) {
                try {
                    latest = configuration.serializer.deserialize(oldData);
                } catch (Exception ignored) {
                    // In the case of an exception encountered during serializing, consider updating as the database data never existed
                    initialQuery.run();
                }
            } else {
                initialQuery.run();
            }
            pickedUpFromDB = true;
        }

        Yuki.LOGGER.info(loggingMarker, "Checking for new versions...");
        final T old = latest;
        T newVersion = null;
        try {
            newVersion = queryLatest();
        } catch (IOException exception) {
            Yuki.LOGGER.error("Encountered exception trying to resolve latest version: ", exception);
        }

        if (newVersion != null && (old == null || configuration.versionComparator.compare(old, newVersion) < 0)) {
            Yuki.LOGGER.info(loggingMarker, "New release found, from {} to {}", old, newVersion);
            update(newVersion);

            final var embed = getEmbed(old, latest);

            // Get all guild ID's of the guilds the bot is in.
            Yuki.getJDA().getGuilds().forEach(guild -> {
                long guildId = guild.getIdLong();
                GuildConfiguration guildConfiguration = Yuki.getInstance().getGuildConfig(guildId);

                // Get channels for this specific guild based on the configuration.
                List<SnowflakeValue> guildChannel = configuration.channelGetter.apply(guildConfiguration);

                if (guildChannel != null && !guildChannel.isEmpty()) {
                    guildChannel.stream()
                            .map(it -> it.resolve(id -> Yuki.getJDA().getTextChannelById(id)))
                            .filter(Objects::nonNull)
                            .forEach(channel -> {
                                embed.setTimestamp(Instant.now());
                                if (configuration.webhookInfo == null) {
                                    channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                                        if (channel.getType() == ChannelType.NEWS) {
                                            msg.crosspost().queue();
                                        }
                                    });
                                } else {
                                    WEBHOOKS.sendAndCrosspost(channel, new WebhookMessageBuilder()
                                            .setAvatarUrl(configuration.webhookInfo.avatarUrl())
                                            .setUsername(configuration.webhookInfo.username())
                                            .addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build())
                                            .build());
                                }
                            });
                }
            });
        }
    }

    private void update(T latest) {
        this.latest = latest;
        Yuki.getInstance().getJdbi().useExtension(UpdateNotifiersDAO.class, db -> db.setLatest(
                configuration.name, configuration.serializer.serialize(latest)
        ));
    }

    @Data
    @Builder
    @SuppressWarnings("ClassCanBeRecord")
    public static final class NotifierConfiguration<T> {
        private final String name;
        private final Function<GuildConfiguration, List<SnowflakeValue>> channelGetter;
        private final Comparator<T> versionComparator;
        private final StringSerializer<T> serializer;
        private final WebhookInfo webhookInfo;

        private NotifierConfiguration(Builder<T> builder) {
            this.name = builder.name;
            this.channelGetter = builder.channelGetter;
            this.versionComparator = builder.versionComparator;
            this.serializer = builder.serializer;
            this.webhookInfo = builder.webhookInfo;
        }

        public static <T> Builder<T> builder() {
            return new Builder<>();
        }

        public static class Builder<T> {
            private String name;
            private Function<GuildConfiguration, List<SnowflakeValue>> channelGetter;
            private Comparator<T> versionComparator;
            private StringSerializer<T> serializer;
            private WebhookInfo webhookInfo;

            public Builder<T> name(String name) {
                this.name = name;
                return this;
            }

            public Builder<T> channelGetter(Function<GuildConfiguration, List<SnowflakeValue>> channelGetter) {
                this.channelGetter = channelGetter;
                return this;
            }

            public Builder<T> versionComparator(Comparator<T> versionComparator) {
                this.versionComparator = versionComparator;
                return this;
            }

            public Builder<T> serializer(StringSerializer<T> serializer) {
                this.serializer = serializer;
                return this;
            }

            public Builder<T> webhookInfo(WebhookInfo webhookInfo) {
                this.webhookInfo = webhookInfo;
                return this;
            }

            public NotifierConfiguration<T> build() {
                return new NotifierConfiguration<>(this);
            }
        }


        public static <T> Comparator<T> notEqual() {
            return (v1, v2) -> v1.equals(v2) ? 0 : -1;
        }
    }

    public record WebhookInfo(String username, String avatarUrl) {
    }
}
