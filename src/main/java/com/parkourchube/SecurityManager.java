package com.parkourchube;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hardened SecurityManager (v2 — HMAC signed, fail-closed).
 * Public API preserved: verify(callback), startHeartbeat(), shutdown(),
 * isApproved(), getServerUuid(), sendPlayerEvent(event, player).
 */
public class SecurityManager {

    private static final String PLUGIN_NAME = "ParkourCube";

    private final ParkourCubePlugin plugin;
    private final int heartbeatMinutes;
    private final String serverUuid;
    private final HttpClient http;
    private final String jarHash;

    private volatile boolean approved = false;
    private BukkitTask heartbeatTask;

    public SecurityManager(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.heartbeatMinutes = plugin.getConfig().getInt("tracker.heartbeat-minutes", 5);
        this.serverUuid = loadOrCreateUuid();
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.jarHash = SecurityUtils.selfJarSha256();
        plugin.getLogger().info("[Tracker] Server UUID: " + serverUuid);
        plugin.getLogger().info("[Tracker] Integrity hash: " + jarHash);
    }

    public boolean isApproved()   { return approved; }
    public String  getServerUuid() { return serverUuid; }

    private String loadOrCreateUuid() {
        File uuidFile = new File(plugin.getDataFolder(), ".server-uuid");
        if (uuidFile.exists()) {
            try { return Files.readString(uuidFile.toPath()).trim(); }
            catch (IOException e) {
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

    /**
     * Fail-closed verification at startup. Invokes callback with true only if the tracker
     * explicitly signed an "approved" status; otherwise false (plugin self-disables).
     */
    public void verify(Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean ok = sendPingAndUpdate("server_start");
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(ok));
        });
    }

    public void startHeartbeat() {
        long intervalTicks = heartbeatMinutes * 60L * 20L;
        heartbeatTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            boolean wasApproved = approved;
            sendPingAndUpdate("heartbeat");
            if (!wasApproved && approved) {
                plugin.getLogger().info("[Tracker] Server has been APPROVED by admin!");
            }
            pollCommands();
        }, intervalTicks, intervalTicks);

        // Faster command poll every 30 s
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pollCommands, 600L, 600L);
    }

    public void shutdown() {
        if (heartbeatTask != null) heartbeatTask.cancel();
        try { sendPingAndUpdate("server_stop"); } catch (Exception ignored) {}
    }

    public void sendPlayerEvent(String event, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String ip = "";
                try {
                    if (player.getAddress() != null) ip = player.getAddress().getAddress().getHostAddress();
                } catch (Exception ignored) {}
                String json = "{" +
                        "\"uuid\":\"" + serverUuid + "\"," +
                        "\"event\":\"" + event + "\"," +
                        "\"player_name\":\"" + escape(player.getName()) + "\"," +
                        "\"player_uuid\":\"" + player.getUniqueId() + "\"," +
                        "\"player_ip\":\"" + ip + "\"," +
                        "\"timestamp\":\"" + Instant.now() + "\"" +
                        "}";
                SecurityUtils.signedPost(http, "/player-event", json, 10);
            } catch (Exception ignored) {}
        });
    }

    // ─── Internals ───────────────────────────────────────────────────────────

    /** Returns true iff ping was signed correctly AND status is "approved". */
    private boolean sendPingAndUpdate(String event) {
        try {
            String hostname;
            try { hostname = java.net.InetAddress.getLocalHost().getHostName(); }
            catch (Exception e) { hostname = Bukkit.getServer().getName(); }

            StringBuilder playersList = new StringBuilder("[");
            Object[] onlinePlayers = Bukkit.getOnlinePlayers().toArray();
            for (int i = 0; i < onlinePlayers.length; i++) {
                Player p = (Player) onlinePlayers[i];
                playersList.append(String.format("{\"name\":\"%s\",\"uuid\":\"%s\"}",
                        escape(p.getName()), p.getUniqueId()));
                if (i < onlinePlayers.length - 1) playersList.append(",");
            }
            playersList.append("]");

            String json = "{" +
                    "\"uuid\":\"" + serverUuid + "\"," +
                    "\"event\":\"" + event + "\"," +
                    "\"server\":\"" + escape(hostname) + "\"," +
                    "\"mc_version\":\"" + Bukkit.getMinecraftVersion() + "\"," +
                    "\"plugin_version\":\"" + plugin.getDescription().getVersion() + "\"," +
                    "\"plugin_name\":\"" + PLUGIN_NAME + "\"," +
                    "\"jar_hash\":\"" + jarHash + "\"," +
                    "\"players\":" + onlinePlayers.length + "," +
                    "\"players_list\":" + playersList + "," +
                    "\"timestamp\":\"" + Instant.now() + "\"" +
                    "}";

            HttpResponse<String> res = SecurityUtils.signedPost(http, "/ping", json, 10);
            boolean sigOk = SecurityUtils.verifyResponse(res);
            String body = res.body();
            plugin.getLogger().info("[Tracker] Ping (" + res.statusCode()
                    + ", sig=" + (sigOk ? "ok" : "BAD") + "): " + body);

            if (!sigOk || res.statusCode() != 200) {
                if (approved) {
                    plugin.getLogger().warning("[Tracker] Signature invalid — revoking approval");
                    approved = false;
                }
                return false;
            }

            Matcher m = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
            String status = m.find() ? m.group(1) : "unknown";
            approved = "approved".equals(status);
            if ("banned".equals(status)) {
                String reason = extract(body, "reason");
                plugin.getLogger().severe("[Tracker] Server is BANNED: " + reason);
            } else if ("pending".equals(status)) {
                plugin.getLogger().warning("[Tracker] Server is PENDING approval. UUID: " + serverUuid);
            } else if ("expired".equals(status)) {
                plugin.getLogger().warning("[Tracker] Approval EXPIRED.");
            }
            return approved;
        } catch (Exception e) {
            plugin.getLogger().warning("[Tracker] Ping failed: " + e.getMessage());
            // Fail-closed: network error does NOT auto-approve. Keep previous state.
            return approved;
        }
    }

    private void pollCommands() {
        try {
            HttpResponse<String> res = SecurityUtils.signedGet(http,
                    "/commands/pending?uuid=" + serverUuid, 10);
            if (res.statusCode() != 200 || !SecurityUtils.verifyResponse(res)) return;
            List<Integer> ids = new ArrayList<>();
            List<String> cmds = new ArrayList<>();
            Matcher m = Pattern.compile(
                    "\"id\"\\s*:\\s*(\\d+).*?\"command\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"",
                    Pattern.DOTALL).matcher(res.body());
            while (m.find()) { ids.add(Integer.parseInt(m.group(1))); cmds.add(m.group(2)); }
            if (ids.isEmpty()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (String c : cmds) {
                    plugin.getLogger().info("[Tracker] Executing remote command: " + c);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
                }
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    try {
                        SecurityUtils.signedPost(http, "/commands/ack",
                                "{\"ids\":" + ids.toString().replace(" ", "") + "}", 10);
                    } catch (Exception ignored) {}
                });
            });
        } catch (Exception ignored) {}
    }

    private static String extract(String body, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
        return m.find() ? m.group(1) : "";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
