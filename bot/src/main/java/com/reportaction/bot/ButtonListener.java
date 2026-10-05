package com.reportaction.bot;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();

        // Format: action:player:reason
        // Examples: ban:Steve:Griefing   tempban7d:Steve:Hacking   mute:Steve:Spam
        if (!id.contains(":")) {
            event.reply("Invalid button data.").setEphemeral(true).queue();
            return;
        }

        String[] parts = id.split(":", 3);
        if (parts.length < 2) {
            event.reply("Invalid button data.").setEphemeral(true).queue();
            return;
        }

        String action = parts[0].toLowerCase();
        String player = parts[1];
        String reason = parts.length > 2 ? parts[2] : "No reason provided";
        String duration = "7d";

        if (action.startsWith("tempban")) {
            if (action.equals("tempban1d")) duration = "1d";
            else if (action.equals("tempban7d")) duration = "7d";
            else if (action.equals("tempban30d")) duration = "30d";
            action = "tempban";
        }

        String staff = event.getUser().getName();

        event.deferReply(true).queue();

        String result = MinecraftBridge.sendPunish(action, player, reason, duration, staff);

        String emoji = switch (action) {
            case "ban" -> "🔨";
            case "tempban" -> "⏳";
            case "mute" -> "🔇";
            case "kick" -> "👢";
            default -> "✅";
        };

        event.getHook().sendMessage(emoji + " **" + action.toUpperCase() + "** executed on `" + player + "`\n" +
                "Reason: " + reason + "\n" +
                "Result: `" + result + "`\n" +
                "By: " + staff).queue();

        // Disable the buttons after use (optional – prevents double clicks)
        try {
            event.getMessage().editMessageComponents(
                    event.getMessage().getActionRows().stream()
                            .map(row -> row.getButtons().stream()
                                    .map(b -> b.asDisabled())
                                    .toList())
                            .map(buttons -> net.dv8tion.jda.api.interactions.components.ActionRow.of(buttons))
                            .toList()
            ).queue(null, err -> {});
        } catch (Exception ignored) {}
    }

    // Helper to create the standard action buttons for a report
    public static java.util.List<Button> createPunishmentButtons(String player, String reason) {
        String safeReason = reason.length() > 80 ? reason.substring(0, 80) : reason;
        // Discord button custom id max length is 100 characters
        String base = player + ":" + safeReason;

        return java.util.List.of(
                Button.danger("ban:" + base, "🔨 Ban"),
                Button.secondary("tempban1d:" + base, "1 Day"),
                Button.secondary("tempban7d:" + base, "7 Days"),
                Button.secondary("tempban30d:" + base, "30 Days"),
                Button.primary("mute:" + base, "🔇 Mute"),
                Button.secondary("kick:" + base, "👢 Kick")
        );
    }
}
