package com.parkourchube;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.function.Consumer;

public class SecurityManager {

    private final ParkourCubePlugin plugin;
    private final String trackerUrl;
    private final String pluginName;
    private final int heartbeatMinutes;
    private String serverUuid;
    private BukkitTask heartbeatTask;
    private volatile boolean approved = false;

    public SecurityManager(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.trackerUrl = plugin.getConfig().getString("tracker.url", "https://mabeltracker.up.railway.app");
        this.pluginName = plugin.getConfig().getString("tracker.plugin-name", "ParkourCube");
        this.heartbeatMinutes = plugin.getConfig().getInt("tracker.heartbeat-minutes", 5);
        this.serverUuid = loadOrCreateUuid();
    }

    private String loadOrCreateUuid() {
        File uuidFile = new File(plugin.getDataFolder(), ".server-uuid");
        if (uuidFile.exists()) {
            try {
                return Files.readString(uuidFile.toPath()).trim();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to read server UUID, generating new one.");
            }
        }
        String uuid = UUID.randomUUID().toString();
        try {
            plugin.getDataFolder().mkdirs();
            Files.writeString(uuidFile.toPath(), uuid);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save server UUID.");
        }
        return uuid;
    }

    public void verify(Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                JsonObject response = sendPing("server_start");
                String status = response.has("status") ? response.get("status").getAsString() : "unknown";

                if ("approved".equals(status)) {
                    approved = true;
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                } else if ("banned".equals(status)) {
                    approved = false;
                    String reason = response.has("reason") ? response.get("reason").getAsString() : "No reason provided";
                    plugin.getLogger().severe("Server is BANNED: " + reason);
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(false));
                } else {
                    approved = false;
                    plugin.getLogger().warning("Server is PENDING approval. UUID: " + serverUuid);
                    plugin.getLogger().warning("Players will be kicked until admin approves this server.");
                    Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not reach tracker: " + e.getMessage());
                plugin.getLogger().warning("Allowing plugin to load (offline mode).");
                Bukkit.getScheduler().runTask(plugin, () -> callback.accept(true));
            }
        });
    }

    public void startHeartbeat() {
        long intervalTicks = heartbeatMinutes * 60L * 20L;
        heartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                JsonObject response = sendPing("heartbeat");
                String status = response.has("status") ? response.get("status").getAsString() : "unknown";
                boolean wasApproved = approved;
                approved = "approved".equals(status);
                if (!wasApproved && approved) {
                    plugin.getLogger().info("[Tracker] Server has been APPROVED by admin!");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Heartbeat failed: " + e.getMessage());
            }
        }, intervalTicks, intervalTicks);
    }

    public void shutdown() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel();
        }
        try {
            sendPing("server_stop");
        } catch (Exception ignored) {}
    }

    private JsonObject sendPing(String event) throws IOException {
        URL url = URI.create(trackerUrl + "/ping").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);

        JsonObject body = new JsonObject();
        body.addProperty("uuid", serverUuid);
        body.addProperty("event", event);
        try {
            body.addProperty("server", java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            body.addProperty("server", Bukkit.getServer().getName());
        }
        body.addProperty("mc_version", Bukkit.getMinecraftVersion());
        body.addProperty("plugin_version", plugin.getDescription().getVersion());
        body.addProperty("plugin_name", pluginName);
        body.addProperty("players", Bukkit.getOnlinePlayers().size());

        // Build players_list array
        StringBuilder playersList = new StringBuilder("[");
        Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();
        for (int i = 0; i < onlinePlayers.length; i++) {
            org.bukkit.entity.Player p = (org.bukkit.entity.Player) onlinePlayers[i];
            playersList.append(String.format("{\"name\":\"%s\",\"uuid\":\"%s\"}",
                    p.getName(), p.getUniqueId().toString()));
            if (i < onlinePlayers.length - 1) playersList.append(",");
        }
        playersList.append("]");

        body.addProperty("timestamp", java.time.Instant.now().toString());

        // Inject players_list as raw JSON array
        String bodyStr = body.toString();
        bodyStr = bodyStr.substring(0, bodyStr.length() - 1) + ",\"players_list\":" + playersList.toString() + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyStr.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
        String responseStr;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            responseStr = sb.toString();
        }
        conn.disconnect();

        return JsonParser.parseString(responseStr).getAsJsonObject();
    }

    public boolean isApproved() { return approved; }

    public String getServerUuid() { return serverUuid; }

    public void sendPlayerEvent(String event, org.bukkit.entity.Player player) {
        if (trackerUrl == null || trackerUrl.isEmpty()) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create(trackerUrl + "/player-event").toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoOutput(true);

                String ip = "";
                try {
                    if (player.getAddress() != null) {
                        ip = player.getAddress().getAddress().getHostAddress();
                    }
                } catch (Exception ignored) {}

                JsonObject body = new JsonObject();
                body.addProperty("uuid", serverUuid);
                body.addProperty("event", event);
                body.addProperty("player_name", player.getName());
                body.addProperty("player_uuid", player.getUniqueId().toString());
                body.addProperty("player_ip", ip);
                body.addProperty("timestamp", java.time.Instant.now().toString());

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("[Tracker] Player event failed: " + e.getMessage());
            }
        });
    }
}
