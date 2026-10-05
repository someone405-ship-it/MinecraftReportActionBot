package com.reportaction.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.awt.Color;
import java.time.Instant;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();

        if (name.equals("report")) {
            String player = event.getOption("player").getAsString();
            String reason = event.getOption("reason").getAsString();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🚨 Player Report")
                    .setColor(Color.RED)
                    .setDescription("A report has been created with action buttons.")
                    .addField("🎯 Reported Player", "`" + player + "`", true)
                    .addField("📝 Reason", reason, false)
                    .addField("👤 Reported by", event.getUser().getAsMention(), true)
                    .setTimestamp(Instant.now())
                    .setFooter("Report Action Bot");

            event.replyEmbeds(embed.build())
                    .addComponents(
                            ActionRow.of(ButtonListener.createPunishmentButtons(player, reason).subList(0, 4)),
                            ActionRow.of(ButtonListener.createPunishmentButtons(player, reason).subList(4, 6))
                    )
                    .queue();
            return;
        }

        if (name.equals("punish")) {
            String player = event.getOption("player").getAsString();
            String action = event.getOption("action").getAsString().toLowerCase();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason";
            String duration = event.getOption("duration") != null ? event.getOption("duration").getAsString() : "7d";

            event.deferReply(true).queue();

            String result = MinecraftBridge.sendPunish(action, player, reason, duration, event.getUser().getName());

            event.getHook().sendMessage("**" + action.toUpperCase() + "** on `" + player + "`\nResult: `" + result + "`").queue();
        }
    }
}
