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

package zone.moddev.patchy.util.webhook;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class WebhookManagerImpl implements WebhookManager {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final Predicate<String> predicate;
    private final String webhookName;
    private final AllowedMentions allowedMentions;
    private final Long2ObjectMap<JDAWebhookClient> webhooks = new Long2ObjectOpenHashMap<>();
    @Nullable
    private final Consumer<Webhook> creationListener;

    public WebhookManagerImpl(final Predicate<String> predicate, final String webhookName, final AllowedMentions allowedMentions, @Nullable final Consumer<Webhook> creationListener) {
        this.predicate = predicate;
        this.webhookName = webhookName;
        this.allowedMentions = allowedMentions;
        this.creationListener = creationListener;
    }

    @Override
    public JDAWebhookClient getWebhook(final IWebhookContainer channel) {
        return webhooks.computeIfAbsent(channel.getIdLong(), k ->
            WebhookClientBuilder.fromJDA(getOrCreateWebhook(channel))
                .setExecutorService(executor)
                .setHttpClient(httpClient)
                .setAllowedMentions(allowedMentions)
                .buildJDA());
    }

    @Override
    public void sendAndCrosspost(final IWebhookContainer channel, final WebhookMessage message) {
        getWebhook(channel)
            .send(message)
            .thenAccept(msg -> {
                if (channel.getType() == ChannelType.NEWS) {
                    ((NewsChannel) channel).retrieveMessageById(msg.getId()).flatMap(Message::crosspost).queue();
                }
            });
    }

    private Webhook getOrCreateWebhook(IWebhookContainer channel) {
        final var alreadyExisted = unwrap(Objects.requireNonNull(channel).retrieveWebhooks()
            .submit(false))
            .stream()
            .filter(w -> predicate.test(w.getName()))
            .findAny();
        return alreadyExisted.orElseGet(() -> {
            final var webhook = unwrap(channel.createWebhook(webhookName).submit(false));
            if (creationListener != null) {
                creationListener.accept(webhook);
            }
            return webhook;
        });
    }

    private static <T> T unwrap(CompletableFuture<T> completableFuture) {
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        webhooks.forEach((id, client) -> client.close());
        executor.shutdown();
    }
}
