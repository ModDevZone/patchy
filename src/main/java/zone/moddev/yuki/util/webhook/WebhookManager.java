package zone.moddev.yuki.util.webhook;

import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessage;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.attribute.IWebhookContainer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface WebhookManager {

    static WebhookManager of(String name) {
        return of(e -> e.trim().equals(name), name, AllowedMentions.none());
    }

    static WebhookManager of(Predicate<String> matcher, String webhookName, AllowedMentions allowedMentions, @Nullable Consumer<Webhook> creationListener) {
        return new WebhookManagerImpl(matcher, webhookName, allowedMentions, creationListener);
    }

    static WebhookManager of(Predicate<String> matcher, String webhookName, AllowedMentions allowedMentions) {
        return of(matcher, webhookName, allowedMentions, null);
    }

    /**
     * Gets or creates the webhook client for a given {@code channel}.
     *
     * @param channel the channel to get or create the webhook in
     * @return the webhook
     */
    JDAWebhookClient getWebhook(IWebhookContainer channel);

    /**
     * Sends and crossposts / publishes a message in a channel using a webhook.
     *
     * @param channel the channel to send the message in
     * @param message the message to send
     */
    void sendAndCrosspost(IWebhookContainer channel, WebhookMessage message);
}
