package com.reportaction;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class ReportActionPlugin extends JavaPlugin {

    private HttpServer server;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int port = getConfig().getInt("port", 8123);
        String secret = getConfig().getString("secret-key", "change-me");

        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/punish", new PunishHandler(secret));
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            getLogger().info("================================================");
            getLogger().info(" ReportActionPlugin enabled");
            getLogger().info(" Listening on port " + port + " for Discord bot actions");
            getLogger().info("================================================");
        } catch (IOException e) {
            getLogger().severe("Failed to start HTTP server on port " + port + ": " + e.getMessage());
            getLogger().severe("Make sure the port is free and try again.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop(0);
            getLogger().info("HTTP server stopped.");
        }
    }

    private class PunishHandler implements HttpHandler {

        private final String secretKey;

        public PunishHandler(String secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method not allowed");
                return;
            }

            String query = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseQuery(query);

            String key = params.getOrDefault("key", "");
            String action = params.getOrDefault("action", "").toLowerCase();
            String player = params.getOrDefault("player", "");
            String reason = params.getOrDefault("reason", "No reason provided");
            String duration = params.getOrDefault("duration", "7d");
            String staff = params.getOrDefault("staff", "Discord");

            if (!key.equals(secretKey)) {
                getLogger().warning("Invalid secret key received from " + exchange.getRemoteAddress());
                sendResponse(exchange, 403, "Invalid key");
                return;
            }

            if (player.isEmpty() || action.isEmpty()) {
                sendResponse(exchange, 400, "Missing player or action");
                return;
            }

            String commandTemplate = getConfig().getString("commands." + action);
            if (commandTemplate == null) {
                sendResponse(exchange, 400, "Unknown action: " + action);
                return;
            }

            final String command = commandTemplate
                    .replace("%player%", player)
                    .replace("%reason%", reason)
                    .replace("%duration%", duration);

            // Run on main thread
            Bukkit.getScheduler().runTask(ReportActionPlugin.this, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                if (getConfig().getBoolean("log-actions", true)) {
                    getLogger().info("[Discord] " + staff + " executed: /" + command);
                }
            });

            sendResponse(exchange, 200, "OK - Command executed: " + command);
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> map = new HashMap<>();
            if (query == null || query.isEmpty()) return map;

            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    try {
                        String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                        String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        map.put(k, v);
                    } catch (Exception ignored) {}
                }
            }
            return map;
        }

        private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}
