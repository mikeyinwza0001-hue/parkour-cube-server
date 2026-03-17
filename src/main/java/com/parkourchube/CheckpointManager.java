package com.parkourchube;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CheckpointManager {

    private final ParkourCubePlugin plugin;

    // Warp locations: checkpoint number -> location
    private final ConcurrentHashMap<Integer, Location> warps = new ConcurrentHashMap<>();

    // Beacon locations: "world:x,y,z" -> checkpoint number
    private final ConcurrentHashMap<String, Integer> beacons = new ConcurrentHashMap<>();

    // Player last checkpoint: player UUID -> checkpoint number
    private final ConcurrentHashMap<UUID, Integer> lastCheckpoint = new ConcurrentHashMap<>();

    // Final checkpoint number
    private volatile int finalCheckpoint = 61;

    // Total checkpoints for display
    private final int totalCheckpoints;

    public CheckpointManager(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.totalCheckpoints = plugin.getConfig().getInt("total-checkpoints", 60);
    }

    public void loadFromApi() {
        String apiUrl = plugin.getConfig().getString("checkpoint-api.url", "http://localhost:6969/api/checkpoints");
        String apiKey = plugin.getConfig().getString("checkpoint-api.key", "CHANGE_ME_TO_A_SECRET_KEY");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create(apiUrl + "?key=" + apiKey).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int code = conn.getResponseCode();
                if (code != 200) {
                    plugin.getLogger().warning("Checkpoint API returned " + code + ". Using empty data.");
                    conn.disconnect();
                    return;
                }

                String body;
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    body = sb.toString();
                }
                conn.disconnect();

                JsonObject data = JsonParser.parseString(body).getAsJsonObject();

                // Load final
                if (data.has("final")) {
                    finalCheckpoint = data.get("final").getAsInt();
                }

                // Load warps
                if (data.has("warps")) {
                    JsonObject warpsObj = data.getAsJsonObject("warps");
                    for (Map.Entry<String, JsonElement> entry : warpsObj.entrySet()) {
                        int id = Integer.parseInt(entry.getKey());
                        JsonObject w = entry.getValue().getAsJsonObject();
                        String worldName = w.has("world") ? w.get("world").getAsString() : "world";

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            World world = Bukkit.getWorld(worldName);
                            if (world == null) world = Bukkit.getWorlds().get(0);
                            Location loc = new Location(world,
                                    w.get("x").getAsDouble(),
                                    w.get("y").getAsDouble(),
                                    w.get("z").getAsDouble(),
                                    w.has("yaw") ? w.get("yaw").getAsFloat() : 0f,
                                    w.has("pitch") ? w.get("pitch").getAsFloat() : 0f);
                            warps.put(id, loc);
                        });
                    }
                }

                // Load beacons
                if (data.has("beacons")) {
                    JsonObject beaconsObj = data.getAsJsonObject("beacons");
                    for (Map.Entry<String, JsonElement> entry : beaconsObj.entrySet()) {
                        beacons.put(entry.getKey(), entry.getValue().getAsInt());
                    }
                }

                int warpCount = data.has("warps") ? data.getAsJsonObject("warps").size() : 0;
                int beaconCount = data.has("beacons") ? data.getAsJsonObject("beacons").size() : 0;
                plugin.getLogger().info("Loaded " + warpCount + " warps, " + beaconCount + " beacons from API.");

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load checkpoints from API: " + e.getMessage());
            }
        });
    }

    // --- Beacon lookup ---

    public String formatBlockLocation(Location loc) {
        return loc.getWorld().getName() + ":"
                + (loc.getBlockX() + 0.5) + ","
                + (loc.getBlockY() + 0.5) + ","
                + (loc.getBlockZ() + 0.5);
    }

    public Integer getBeaconCheckpoint(Location blockLoc) {
        return beacons.get(formatBlockLocation(blockLoc));
    }

    public void registerBeacon(Location blockLoc, int cpNumber) {
        beacons.put(formatBlockLocation(blockLoc), cpNumber);
    }

    // --- Warps ---

    public Location getWarp(int cpNumber) {
        return warps.get(cpNumber);
    }

    public void setWarp(int cpNumber, Location loc) {
        warps.put(cpNumber, loc.clone());
    }

    public boolean hasWarp(int cpNumber) {
        return warps.containsKey(cpNumber);
    }

    // --- Player checkpoint ---

    public int getLastCheckpoint(UUID playerUuid) {
        return lastCheckpoint.getOrDefault(playerUuid, 0);
    }

    public void setLastCheckpoint(UUID playerUuid, int cp) {
        lastCheckpoint.put(playerUuid, cp);
    }

    public boolean hasLastCheckpoint(UUID playerUuid) {
        return lastCheckpoint.containsKey(playerUuid);
    }

    // --- Final CP ---

    public int getFinalCheckpoint() { return finalCheckpoint; }
    public void setFinalCheckpoint(int cp) { this.finalCheckpoint = cp; }
    public int getTotalCheckpoints() { return totalCheckpoints; }

    // --- Export ---

    public Map<Integer, Location> getWarps() { return warps; }
    public Map<String, Integer> getBeacons() { return beacons; }
}
