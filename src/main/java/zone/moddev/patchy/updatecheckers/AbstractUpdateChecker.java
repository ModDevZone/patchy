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

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel;
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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractUpdateChecker<T> implements Runnable {
    public static final String WEBHOOK_NAME = "UpdateNotifiers";
    protected static final WebhookManager WEBHOOKS = WebhookManager.of(s -> s.trim().equals(WEBHOOK_NAME), WEBHOOK_NAME, AllowedMentions.none());

    protected final NotifierConfiguration<T> configuration;
    protected final Marker loggingMarker;
    private T latest;
    private boolean pickedUpFromDB;

    protected AbstractUpdateChecker(final NotifierConfiguration<T> configuration) {
        this.configuration = configuration;
        this.loggingMarker = MarkerFactory.getMarker(configuration.name);
    }

    @Nullable
    protected abstract T queryLatest() throws IOException;

    @NotNull
    protected abstract List<EmbedBuilder> getEmbeds(@Nullable T oldVersion, T newVersion);

    @Override
    public final void run() {
        if (Patchy.getInstance() == null) {
            Patchy.LOGGER.warn(loggingMarker, "Cannot start {} update notifier due to the bot instance being null.", configuration.name);
            return;
        }
        if (!pickedUpFromDB) {
            final String oldData = Patchy.getInstance().getJdbi()
                .withExtension(UpdateCheckerDAO.class, db -> db.getLatest(configuration.name));
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
                        return Patchy.getInstance().getConfigManager().loadOrCreateGuildConfig(guild.getId());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            configuration.channelGetter.apply(guildConfigs)
                .stream()
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
                            WEBHOOKS.sendAndCrosspost(channel, new WebhookMessageBuilder()
                                .setAvatarUrl(configuration.webhookInfo.avatarUrl())
                                .setUsername(configuration.webhookInfo.username())
                                .addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build())
                                .build());
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
            configuration.name, configuration.serializer.serialize(latest)
        ));
    }

    public static final class NotifierConfiguration<T> {
        private final String name;
        private final UpdateCheckerType type;
        private final Function<List<GuildConfig>, List<String>> channelGetter;
        private final Comparator<T> versionComparator;
        private final StringSerializer<T> serializer;
        private final WebhookInfo webhookInfo;

        private NotifierConfiguration(String name, UpdateCheckerType type, Function<List<GuildConfig>, List<String>> channelGetter, Comparator<T> versionComparator, StringSerializer<T> serializer, WebhookInfo webhookInfo) {
            this.name = name;
            this.type = type;
            this.channelGetter = channelGetter;
            this.versionComparator = versionComparator;
            this.serializer = serializer;
            this.webhookInfo = webhookInfo;
        }

        public static <T> NotifierConfigurationBuilder<T> builder() {
            return new NotifierConfigurationBuilder<>();
        }

        public String getName() {
            return this.name;
        }

        public UpdateCheckerType getType() {
            return this.type;
        }

        public Function<List<GuildConfig>, List<String>> getChannelGetter() {
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
            final Object this$name = this.getName();
            final Object other$name = other.getName();
            if (!Objects.equals(this$name, other$name)) return false;
            final Object this$type = this.getType();
            final Object other$type = other.getType();
            if (!Objects.equals(this$type, other$type)) return false;
            final Object this$channelGetter = this.getChannelGetter();
            final Object other$channelGetter = other.getChannelGetter();
            if (!Objects.equals(this$channelGetter, other$channelGetter)) return false;
            final Object this$versionComparator = this.getVersionComparator();
            final Object other$versionComparator = other.getVersionComparator();
            if (!Objects.equals(this$versionComparator, other$versionComparator)) return false;
            final Object this$serializer = this.getSerializer();
            final Object other$serializer = other.getSerializer();
            if (!Objects.equals(this$serializer, other$serializer)) return false;
            final Object this$webhookInfo = this.getWebhookInfo();
            final Object other$webhookInfo = other.getWebhookInfo();
            return Objects.equals(this$webhookInfo, other$webhookInfo);
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $name = this.getName();
            result = result * PRIME + ($name == null ? 43 : $name.hashCode());
            final Object $type = this.getType();
            result = result * PRIME + ($type == null ? 43 : $type.hashCode());
            final Object $channelGetter = this.getChannelGetter();
            result = result * PRIME + ($channelGetter == null ? 43 : $channelGetter.hashCode());
            final Object $versionComparator = this.getVersionComparator();
            result = result * PRIME + ($versionComparator == null ? 43 : $versionComparator.hashCode());
            final Object $serializer = this.getSerializer();
            result = result * PRIME + ($serializer == null ? 43 : $serializer.hashCode());
            final Object $webhookInfo = this.getWebhookInfo();
            result = result * PRIME + ($webhookInfo == null ? 43 : $webhookInfo.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "AbstractUpdateChecker.NotifierConfiguration(name=" + this.getName() + ", type=" + this.getType() + ", channelGetter=" + this.getChannelGetter() + ", versionComparator=" + this.getVersionComparator() + ", serializer=" + this.getSerializer() + ", webhookInfo=" + this.getWebhookInfo() + ")";
        }

        public static <T> Comparator<T> notEqual() {
            return (v1, v2) -> v1.equals(v2) ? 0 : -1;
        }

        public static class NotifierConfigurationBuilder<T> {
            private String name;
            private UpdateCheckerType type;
            private Function<List<GuildConfig>, List<String>> channelGetter;
            private Comparator<T> versionComparator;
            private StringSerializer<T> serializer;
            private WebhookInfo webhookInfo;

            NotifierConfigurationBuilder() {
            }

            public NotifierConfigurationBuilder<T> name(String name) {
                this.name = name;
                return this;
            }

            public NotifierConfigurationBuilder<T> type(UpdateCheckerType type) {
                this.type = type;
                return this;
            }

            public NotifierConfigurationBuilder<T> channelGetter(Function<List<GuildConfig>, List<String>> channelGetter) {
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
                            .map(config -> config.getChannelId(this.type))
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                }
                Objects.requireNonNull(this.channelGetter, "channelGetter cannot be null. Did you forget to set the type?");
                return new NotifierConfiguration<>(name, type, channelGetter, versionComparator, serializer, webhookInfo);
            }

            public String toString() {
                return "AbstractUpdateChecker.NotifierConfiguration.NotifierConfigurationBuilder(name=" + this.name + ", type=" + this.type + ", channelGetter=" + this.channelGetter + ", versionComparator=" + this.versionComparator + ", serializer=" + this.serializer + ", webhookInfo=" + this.webhookInfo + ")";
            }
        }
    }

    public record WebhookInfo(String username, String avatarUrl) {
    }
}
