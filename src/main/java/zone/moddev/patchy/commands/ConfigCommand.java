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

package zone.moddev.patchy.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import zone.moddev.patchy.Patchy;
import zone.moddev.patchy.configs.GuildConfig;
import zone.moddev.patchy.updatecheckers.UpdateCheckerType;

import java.io.IOException;

public class ConfigCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("patchy-config")) {
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("Please specify a subcommand.").setEphemeral(true).queue();
            return;
        }

        try {
            GuildConfig config = Patchy.getInstance().getConfigManager().loadOrCreateGuildConfig(event.getGuild());
            if (!hasPermission(event.getMember(), config)) {
                event.reply("You do not have permission to do that.").setEphemeral(true).queue();
                return;
            }

            switch (subcommand) {
                case "set" -> {
                    String typeStr = event.getOption("type").getAsString();
                    UpdateCheckerType type = UpdateCheckerType.valueOf(typeStr.toUpperCase());
                    GuildChannel channel = event.getOption("channel").getAsChannel();
                    setChannel(config, type, channel.getId());
                    Patchy.getInstance().getConfigManager().saveGuildConfig(event.getGuild().getId(), config);
                    event.reply("Notification channel for " + typeStr + " updates has been set to " + channel.getAsMention()).queue();
                }

                case "unset" -> {
                    String typeStr = event.getOption("type").getAsString();
                    UpdateCheckerType type = UpdateCheckerType.valueOf(typeStr.toUpperCase());
                    setChannel(config, type, null);
                    Patchy.getInstance().getConfigManager().saveGuildConfig(event.getGuild().getId(), config);
                    event.reply("Notifications for " + typeStr + " updates have been disabled.").queue();
                }

                case "view" -> {
                    StringBuilder sb = new StringBuilder("Current update notification channels:\n");
                    for (UpdateCheckerType type : UpdateCheckerType.values()) {
                        String channelId = getChannel(config, type);
                        if (channelId != null) {
                            sb.append(type.name()).append(": <#").append(channelId).append(">\n");
                        } else {
                            sb.append(type.name()).append(": Not set\n");
                        }
                    }
                    event.reply(sb.toString()).queue();
                }

                case "set-role" -> {
                    Role role = event.getOption("role").getAsRole();
                    config.setBotControllerRoleId(role.getId());
                    Patchy.getInstance().getConfigManager().saveGuildConfig(event.getGuild().getId(), config);
                    event.reply("Bot controller role has been set to " + role.getAsMention()).queue();
                }
            }
        } catch (IOException exception) {
            event.reply("Failed to load or save configuration. Please try again or contact the bots owner.").setEphemeral(true).queue();
        }
    }

    private boolean hasPermission(Member member, GuildConfig config) {
        if (member == null) {
            return false;
        }

        if (member.hasPermission(Permission.MANAGE_SERVER)) {
            return true;
        }

        String roleId = config.getBotControllerRoleId();
        if (roleId != null) {
            return member.getRoles().stream().anyMatch(role -> role.getId().equals(roleId));
        }

        return false;
    }

    private void setChannel(GuildConfig config, UpdateCheckerType type, String channelId) {
        switch (type) {
            case MINECRAFT -> config.setMinecraftNewsChannelId(channelId);
            case BLOCKBENCH -> config.setBlockbenchNewsChannelId(channelId);
            case NEOFORGE -> config.setNeoForgeNewsChannelId(channelId);
            case FORGE -> config.setForgeNewsChannelId(channelId);
            case PARCHMENT -> config.setParchmentNewsChannelId(channelId);
            case FABRIC -> config.setFabricNewsChannelId(channelId);
        }
    }

    private String getChannel(GuildConfig config, UpdateCheckerType type) {
        return switch (type) {
            case MINECRAFT -> config.getMinecraftNewsChannelId();
            case BLOCKBENCH -> config.getBlockbenchNewsChannelId();
            case NEOFORGE -> config.getNeoForgeNewsChannelId();
            case FORGE -> config.getForgeNewsChannelId();
            case PARCHMENT -> config.getParchmentNewsChannelId();
            case FABRIC -> config.getFabricNewsChannelId();
        };
    }
}
