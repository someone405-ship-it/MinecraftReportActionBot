package com.reportaction.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class ReportActionBot {

    public static String BOT_TOKEN;
    public static String SECRET_KEY;
    public static String MC_HOST;
    public static int MC_PORT;
    public static String REPORT_CHANNEL_ID;

    public static void main(String[] args) throws Exception {
        loadConfig();

        if (BOT_TOKEN == null || BOT_TOKEN.contains("PUT_YOUR")) {
            System.err.println("ERROR: Please set a valid bot-token in bot/config.yml");
            System.exit(1);
        }

        JDA jda = JDABuilder.createDefault(BOT_TOKEN)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.watching("reports"))
                .addEventListeners(new ButtonListener(), new SlashCommandListener())
                .build();

        jda.awaitReady();

        // Register slash commands
        jda.updateCommands().addCommands(
                Commands.slash("report", "Create a report with action buttons")
                        .addOption(OptionType.STRING, "player", "Player to report", true)
                        .addOption(OptionType.STRING, "reason", "Reason for the report", true),
                Commands.slash("punish", "Quick punish a player")
                        .addOption(OptionType.STRING, "player", "Player name", true)
                        .addOption(OptionType.STRING, "action", "ban / tempban / mute / kick", true)
                        .addOption(OptionType.STRING, "reason", "Reason", false)
                        .addOption(OptionType.STRING, "duration", "Duration for tempban (e.g. 7d)", false)
        ).queue();

        System.out.println("================================================");
        System.out.println(" Report Action Bot is online!");
        System.out.println(" Minecraft target: " + MC_HOST + ":" + MC_PORT);
        System.out.println("================================================");
    }

    @SuppressWarnings("unchecked")
    private static void loadConfig() throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream("config.yml")) {
            Map<String, Object> config = yaml.load(in);
            BOT_TOKEN = String.valueOf(config.get("bot-token"));
            SECRET_KEY = String.valueOf(config.get("secret-key"));
            MC_HOST = String.valueOf(config.get("minecraft-host"));
            MC_PORT = Integer.parseInt(String.valueOf(config.get("minecraft-port")));
            Object channel = config.get("report-channel-id");
            REPORT_CHANNEL_ID = channel != null ? String.valueOf(channel) : "";
        }
    }
}
