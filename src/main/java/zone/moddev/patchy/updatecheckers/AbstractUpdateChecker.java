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

package zone.moddev.patchy.updatecheckers;

import com.google.common.base.MoreObjects;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.configs.GuildConfig;
import zone.moddev.patchy.util.StringSerializer;
import zone.moddev.patchy.util.dao.UpdateCheckerDAO;
import zone.moddev.patchy.util.webhook.WebhookManager;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractUpdateChecker<T> implements Runnable {
    public static final String WEBHOOK_NAME = "UpdateCheckers";
    protected static final WebhookManager WEBHOOKS = WebhookManager.of(s -> s.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, Collections.emptyList());

    protected final NotifierConfiguration<T> configuration;
    protected final Marker loggingMarker;
    private T latest;
    private boolean pickedUpFromDB;

    protected AbstractUpdateChecker(final NotifierConfiguration<T> configuration) {
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.getType().getName());
    }

    @Nullable
    protected abstract T queryLatest() throws IOException;

    @NotNull
    protected abstract List<EmbedBuilder> getEmbeds(@Nullable T oldVersion, T newVersion);

    @Override
    public final void run() {
        if (Patchy.getInstance() == null) {
            Patchy.LOGGER.warn(loggingMarker, "Cannot start {} update notifier due to the bot instance being null.", configuration.getType());
            return;
        }
        if (!pickedUpFromDB) {
            final String oldData = Patchy.getInstance().getJdbi()
                    .withExtension(UpdateCheckerDAO.class, db -> db.getLatest(configuration.getType().getName()));
            final Runnable initialQuery = () -> {
                try {
                    final T queried = queryLatest();
                    if (queried != null) {
                        update(queried);
                    }
                } catch (IOException e) {
                    Patchy.LOGGER.error(loggingMarker, "An exception occurred trying to resolve latest version: ", e);
                }
            };
            if (oldData != null) {
                try {
                    latest = configuration.serializer.deserialize(oldData);
                } catch (Exception ignored) {
                    initialQuery.run();
                }
            } else {
                initialQuery.run();
            }
            pickedUpFromDB = true;
        }

        final T old = latest;
        T newVersion = null;
        try {
            newVersion = queryLatest();
        } catch (IOException e) {
            Patchy.LOGGER.error("Encountered exception trying to resolve latest version: ", e);
        }

        if (newVersion != null && (old == null || configuration.versionComparator.compare(old, newVersion) < 0)) {
            update(newVersion);

            final var embeds = getEmbeds(old, latest);

            if (embeds.isEmpty()) {
                return;
            }

            final List<GuildConfig> guildConfigs = Patchy.getJDA().getGuilds().stream()
                    .map(guild -> {
                        try {
                            return Patchy.getInstance().getConfigManager().loadOrCreateGuildConfig(guild);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            configuration.getNotificationChannelsFromGuilds(guildConfigs)
                    .map(it -> Patchy.getJDA().getChannelById(StandardGuildMessageChannel.class, it))
                    .filter(Objects::nonNull)
                    .forEach(channel -> {
                        for (final var embed : embeds) {
                            embed.setTimestamp(Instant.now());
                            if (configuration.webhookInfo == null) {
                                channel.sendMessageEmbeds(embed.build()).queue(msg -> {
                                    if (channel.getType() == ChannelType.NEWS) {
                                        msg.crosspost().queue();
                                    }
                                });
                            } else {
                                WEBHOOKS.sendAndCrosspost(channel,
                                        configuration.webhookInfo.username(),
                                        configuration.webhookInfo.avatarUrl(),
                                        new MessageCreateBuilder().addEmbeds(embed.build()).build()
                                );
                            }
                        }
                    });
        } else {
            Patchy.LOGGER.debug(loggingMarker, "No new version found");
        }
    }

    private void update(T latest) {
        this.latest = latest;
        Patchy.getInstance().getJdbi().useExtension(UpdateCheckerDAO.class, db -> db.setLatest(
                configuration.getType().getName(), configuration.serializer.serialize(latest)
        ));
    }

    public static final class NotifierConfiguration<T> {
        private final UpdateCheckerType type;
        private final Function<List<GuildConfig>, Stream<String>> channelGetter;
        private final Comparator<T> versionComparator;
        private final StringSerializer<T> serializer;
        private final WebhookInfo webhookInfo;

        private NotifierConfiguration(UpdateCheckerType type, Function<List<GuildConfig>, Stream<String>> channelGetter, Comparator<T> versionComparator, StringSerializer<T> serializer, WebhookInfo webhookInfo) {
            this.type = type;
            this.channelGetter = channelGetter;
            this.versionComparator = versionComparator;
            this.serializer = serializer;
            this.webhookInfo = webhookInfo;
        }

        public static <T> NotifierConfigurationBuilder<T> builder() {
            return new NotifierConfigurationBuilder<>();
        }

        public UpdateCheckerType getType() {
            return this.type;
        }

        public Stream<String> getNotificationChannelsFromGuilds(List<GuildConfig> guilds) {
            return channelGetter.apply(guilds);
        }

        public Function<List<GuildConfig>, Stream<String>> getChannelGetter() {
            return this.channelGetter;
        }

        public Comparator<T> getVersionComparator() {
            return this.versionComparator;
        }

        public StringSerializer<T> getSerializer() {
            return this.serializer;
        }

        public WebhookInfo getWebhookInfo() {
            return this.webhookInfo;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) return true;
            if (!(o instanceof final NotifierConfiguration<?> other)) return false;
            return this.type == other.type
                    && Objects.equals(this.channelGetter, other.channelGetter)
                    && Objects.equals(this.versionComparator, other.versionComparator)
                    && Objects.equals(this.serializer, other.serializer)
                    && Objects.equals(this.webhookInfo, other.webhookInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, channelGetter, versionComparator, serializer, webhookInfo);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("type", this.type)
                    .add("channelGetter", this.channelGetter)
                    .add("versionComparator", this.versionComparator)
                    .add("serializer", this.serializer)
                    .add("webhookInfo", this.webhookInfo)
                    .toString();
        }

        public static <T> Comparator<T> notEqual() {
            return (v1, v2) -> v1.equals(v2) ? 0 : -1;
        }

        public static class NotifierConfigurationBuilder<T> {
            private UpdateCheckerType type;
            private Function<List<GuildConfig>, Stream<String>> channelGetter;
            private Comparator<T> versionComparator;
            private StringSerializer<T> serializer;
            private WebhookInfo webhookInfo;

            NotifierConfigurationBuilder() {
            }

            public NotifierConfigurationBuilder<T> type(UpdateCheckerType type) {
                this.type = type;
                return this;
            }

            public NotifierConfigurationBuilder<T> channelGetter(Function<List<GuildConfig>, Stream<String>> channelGetter) {
                this.channelGetter = channelGetter;
                return this;
            }

            public NotifierConfigurationBuilder<T> versionComparator(Comparator<T> versionComparator) {
                this.versionComparator = versionComparator;
                return this;
            }

            public NotifierConfigurationBuilder<T> serializer(StringSerializer<T> serializer) {
                this.serializer = serializer;
                return this;
            }

            public NotifierConfigurationBuilder<T> webhookInfo(WebhookInfo webhookInfo) {
                this.webhookInfo = webhookInfo;
                return this;
            }

            public NotifierConfiguration<T> build() {
                if (this.channelGetter == null && this.type != null) {
                    this.channelGetter = configs -> configs.stream()
                            .map(config -> config.getChannelId(this.type.getChannelType()))
                            .filter(Objects::nonNull)
                            .distinct();
                }
                Objects.requireNonNull(this.channelGetter, "channelGetter cannot be null. Did you forget to set the type?");
                return new NotifierConfiguration<>(type, channelGetter, versionComparator, serializer, webhookInfo);
            }

            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("type", this.type)
                        .add("channelGetter", this.channelGetter)
                        .add("versionComparator", this.versionComparator)
                        .add("serializer", this.serializer)
                        .add("webhookInfo", this.webhookInfo)
                        .toString();
            }
        }
    }

    public record WebhookInfo(String username, String avatarUrl) {
    }
}
