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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.configs.GuildConfig;
import zone.moddev.patchy.util.Constants;
import zone.moddev.patchy.util.dao.UpdateCheckerDAO;
import zone.moddev.patchy.util.webhook.WebhookManager;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractUpdateChecker<T> implements Runnable {
    public static final String WEBHOOK_NAME = "UpdateCheckers";
    protected static final WebhookManager WEBHOOKS = WebhookManager.of(s -> s.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, Collections.emptyList());

    protected final NotifierConfiguration<T> configuration;
    protected final Marker loggingMarker;
    protected final TypeToken<T> type;
    private final AtomicBoolean pickedUpFromDB = new AtomicBoolean(false);
    private final Map<String, T> latest = new HashMap<>(1);

    protected AbstractUpdateChecker(final TypeToken<T> type, final NotifierConfiguration<T> configuration) {
        this.type = type;
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.getType().getName());
    }

    protected AbstractUpdateChecker(final Class<T> type, final NotifierConfiguration<T> configuration) {
        this(TypeToken.get(type), configuration);
    }

    protected abstract List<String> getUpdateKeys() throws IOException;

    protected abstract Map<String, T> fetchLatest() throws IOException;

    protected abstract List<EmbedBuilder> getEmbeds(String key, @Nullable T oldVersion, T newVersion) throws IOException;

    @Override
    public final void run() {
        if (Patchy.getInstance() == null) {
            Patchy.LOGGER.warn(loggingMarker, "Cannot start {} update notifier due to the bot instance being null.", configuration.getType().getName());
            return;
        }

        List<String> keys;
        try {
            keys = getUpdateKeys();
        } catch (IOException e) {
            Patchy.LOGGER.error(loggingMarker, "Unable to resolve version keys", e);
            return;
        }

        List<UpdateResult<T>> toUpdate = Patchy.getInstance().getJdbi().inTransaction(handle -> {
            try {
                var dbUpdate = handle.attach(UpdateCheckerDAO.class);

                if (!pickedUpFromDB.getAndSet(true)) {
                    var oldData = new HashMap<String, String>();
                    keys.forEach(key -> {
                        var value = dbUpdate.getLatest(configuration.getType(), key);
                        if(value != null) {
                            oldData.put(key, value);
                        }
                    });

                    try {
                        oldData.forEach((key, value) -> {
                            if (keys.contains(key)) {
                                try {
                                    var deserialized = Constants.GSON.fromJson(value, getTypeForSerialization());
                                    latest.put(key, deserialized);
                                } catch (JsonSyntaxException jse) {
                                    Patchy.LOGGER.debug("Deserialization error: {}\n{}", getTypeForSerialization(), value, jse);
                                }
                            }
                        });
                    } catch (Exception e) {
                        Patchy.LOGGER.debug(loggingMarker, "Unable to deserialize stored data, downloading new version", e);
                        try {
                            var queried = fetchLatest();
                            batchUpdate(queried, dbUpdate);
                        } catch (IOException e2) {
                            Patchy.LOGGER.error(loggingMarker, "An exception occurred trying to resolve latest version: ", e2);
                            return List.of();
                        }
                    }
                }

                var results = new ArrayList<UpdateResult<T>>();
                try {
                    var newData = fetchLatest();

                    keys.forEach(key -> {
                        var oldValue = latest.get(key);
                        var newValue = newData.get(key);

                        if (newValue != null && (oldValue == null || configuration.getVersionComparator().compare(oldValue, newValue) < 0)) {
                            results.add(new UpdateResult<>(key, newValue, oldValue));
                        }
                    });
                } catch (IOException e) {
                    Patchy.LOGGER.error(loggingMarker, "Encountered exception trying to resolve latest version: ", e);
                }

                batchUpdate(results.stream().collect(Collectors.toMap(UpdateResult::key, UpdateResult::newValue)), dbUpdate);

                return results;
            }
            catch (Throwable t) {
                Patchy.LOGGER.error(loggingMarker, "Error while refreshing versions", t);
                return List.of();
            }
        });

        if(!toUpdate.isEmpty()) {
            Patchy.LOGGER.info(loggingMarker, "Found new version(s):\n\t{}", toUpdate.stream().map(Objects::toString).collect(Collectors.joining("\n\t")));
        }

        var embeds = toUpdate.stream()
                .flatMap(res -> {
                    try {
                        return getEmbeds(res.key(), res.oldValue(), res.newValue()).stream();
                    } catch (IOException e) {
                        Patchy.LOGGER.error(loggingMarker, "Unable to build embed for {}/{}", res.key(), res.newValue(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (embeds.isEmpty()) {
            Patchy.LOGGER.debug(loggingMarker, "No new version found");
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
    }

    protected TypeToken<T> getTypeForSerialization() {
        return this.type;
    }

    private void batchUpdate(Map<String, T> queried, UpdateCheckerDAO dbUpdate) {
        latest.putAll(queried);
        var keys = new ArrayList<String>();
        var versions = new ArrayList<String>();
        var raw = new ArrayList<String>();
        queried.forEach((key, version) -> {
            keys.add(key);
            versions.add(configuration.getVersionKeyExtractor().apply(version));
            raw.add(Constants.GSON.toJson(version));
        });
        dbUpdate.batchUpdate(configuration.getType(), keys, versions, raw);
    }

    public record UpdateResult<T>(String key, T newValue, @Nullable T oldValue) {
    }

    public static abstract class Single<T> extends AbstractUpdateChecker<T> {

        protected Single(TypeToken<T> type, NotifierConfiguration<T> configuration) {
            super(type, configuration);
        }

        protected Single(Class<T> type, NotifierConfiguration<T> configuration) {
            super(type, configuration);
        }

        @Override
        protected final List<String> getUpdateKeys() {
            return List.of(configuration.type.getName());
        }

        @Override
        protected final Map<String, T> fetchLatest() throws IOException {
            var t = fetchLatestSingle();
            return t != null ? Map.of(configuration.getType().getName(), t) : Map.of();
        }

        @Nullable
        protected abstract T fetchLatestSingle() throws IOException;

        @Override
        protected final List<EmbedBuilder> getEmbeds(String _key, @Nullable T oldVersion, T newVersion) {
            return getEmbedsSingle(oldVersion, newVersion);
        }

        protected abstract List<EmbedBuilder> getEmbedsSingle(@Nullable T oldVersion, T newVersion);
    }

    public static final class NotifierConfiguration<T> {
        private final UpdateCheckerType type;
        private final Function<List<GuildConfig>, Stream<String>> channelGetter;
        private final Comparator<T> versionComparator;
        private final Function<T, String> versionKeyExtractor;
        private final WebhookInfo webhookInfo;

        private NotifierConfiguration(UpdateCheckerType type, Function<List<GuildConfig>, Stream<String>> channelGetter, Comparator<T> versionComparator, Function<T, String> versionKeyExtractor, WebhookInfo webhookInfo) {
            this.type = type;
            this.channelGetter = channelGetter;
            this.versionComparator = versionComparator;
            this.versionKeyExtractor = versionKeyExtractor;
            this.webhookInfo = webhookInfo;
        }

        public static <T> NotifierConfigurationBuilder<T> builder(UpdateCheckerType type) {
            return new NotifierConfigurationBuilder<>(type);
        }

        public static <T> Comparator<T> notEqual() {
            return (v1, v2) -> v1.equals(v2) ? 0 : -1;
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

        public Function<T, String> getVersionKeyExtractor() {
            return versionKeyExtractor;
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
                    && Objects.equals(this.versionKeyExtractor, other.versionKeyExtractor)
                    && Objects.equals(this.webhookInfo, other.webhookInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, channelGetter, versionComparator, versionKeyExtractor, webhookInfo);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("type", this.type)
                    .add("channelGetter", this.channelGetter)
                    .add("versionComparator", this.versionComparator)
                    .add("versionKeyExtractor", this.versionKeyExtractor)
                    .add("webhookInfo", this.webhookInfo)
                    .toString();
        }

        public static class NotifierConfigurationBuilder<T> {
            private UpdateCheckerType type;
            private Function<List<GuildConfig>, Stream<String>> channelGetter = configs -> configs.stream()
                    .map(config -> config.getChannelId(this.type.getChannelType()))
                    .filter(Objects::nonNull)
                    .distinct();
            private Comparator<T> versionComparator;
            private Function<T, String> versionKeyExtractor = Object::toString;
            private WebhookInfo webhookInfo;

            private NotifierConfigurationBuilder(UpdateCheckerType type) {
                this.type = type;
            }

            public NotifierConfigurationBuilder<T> channelGetter(Function<List<GuildConfig>, Stream<String>> channelGetter) {
                this.channelGetter = channelGetter;
                return this;
            }

            public NotifierConfigurationBuilder<T> versionComparator(Comparator<T> versionComparator) {
                this.versionComparator = versionComparator;
                return this;
            }

            public NotifierConfigurationBuilder<T> versionKeyExtractor(Function<T, String> versionKeyExtractor) {
                this.versionKeyExtractor = versionKeyExtractor;
                return this;
            }

            public NotifierConfigurationBuilder<T> webhookInfo(WebhookInfo webhookInfo) {
                this.webhookInfo = webhookInfo;
                return this;
            }

            public NotifierConfiguration<T> build() {
                return new NotifierConfiguration<>(type, channelGetter, versionComparator, versionKeyExtractor, webhookInfo);
            }

            public String toString() {
                return MoreObjects.toStringHelper(this)
                        .add("type", this.type)
                        .add("channelGetter", this.channelGetter)
                        .add("versionComparator", this.versionComparator)
                        .add("versionKeyExtractor", this.versionKeyExtractor)
                        .add("webhookInfo", this.webhookInfo)
                        .toString();
            }
        }
    }

    public record WebhookInfo(String username, String avatarUrl) {
    }
}
