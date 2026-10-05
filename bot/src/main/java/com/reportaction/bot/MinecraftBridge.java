package com.reportaction.bot;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MinecraftBridge {

    public static String sendPunish(String action, String player, String reason, String duration, String staff) {
        try {
            String urlStr = "http://" + ReportActionBot.MC_HOST + ":" + ReportActionBot.MC_PORT + "/punish";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String body = "key=" + encode(ReportActionBot.SECRET_KEY)
                    + "&action=" + encode(action)
                    + "&player=" + encode(player)
                    + "&reason=" + encode(reason)
                    + "&duration=" + encode(duration)
                    + "&staff=" + encode(staff);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String response = new String(
                    (code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream())
                            .readAllBytes(), StandardCharsets.UTF_8);

            conn.disconnect();
            return code + " - " + response;

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private static String encode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
