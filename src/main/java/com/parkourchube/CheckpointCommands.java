package com.parkourchube;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import static com.parkourchube.PlayerListener.showTitle;

public class CheckpointCommands {

    private final ParkourCubePlugin plugin;
    private final CheckpointManager cpm;
    private final OverlayBridge overlay;

    public CheckpointCommands(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.cpm = plugin.getCheckpointManager();
        this.overlay = plugin.getOverlayBridge();
    }

    // ─── /registercp <number> ───────────────────────────────────────────────

    public boolean registerCp(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        int cpNum;
        try { cpNum = Integer.parseInt(args[0]); } catch (NumberFormatException e) { return false; }

        if (sender instanceof Player player) {
            Block below = player.getLocation().subtract(0, 1, 0).getBlock();
            if (below.getType() != org.bukkit.Material.BEACON) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                player.sendMessage(PlayerListener.colorize("&cNo Beacon &7- Stand on a Beacon block"));
                return true;
            }
            cpm.registerBeacon(below.getLocation(), cpNum);
            cpm.setWarp(cpNum, player.getLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
            player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5);
            player.sendMessage(PlayerListener.colorize("&aCP " + cpNum + " Set &7- Beacon registered"));
        } else {
            sender.sendMessage("This command must be run by a player.");
        }
        return true;
    }

    // ─── /setfinalcp <number> ───────────────────────────────────────────────

    public boolean setFinalCp(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        int cpNum;
        try { cpNum = Integer.parseInt(args[0]); } catch (NumberFormatException e) { return false; }

        cpm.setFinalCheckpoint(cpNum);
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 80, 0.5, 1, 0.5);
            player.sendMessage(PlayerListener.colorize("&6Final Set &eCP " + cpNum + " is now the goal"));
        } else {
            sender.sendMessage("Final CP set to " + cpNum);
        }
        return true;
    }

    // ─── /back [number] ─────────────────────────────────────────────────────

    public boolean back(String[] args) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!cpm.hasLastCheckpoint(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cNo CP", "&7No checkpoint saved");
                continue;
            }

            int current = cpm.getLastCheckpoint(player.getUniqueId());
            int target;
            if (args.length > 0) {
                try { target = Integer.parseInt(args[0]); } catch (NumberFormatException e) { continue; }
            } else {
                target = current - 1;
            }

            if (target <= 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cNone", "&7No earlier checkpoint");
                continue;
            }
            if (target > current) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cLocked", "&7Haven't reached CP " + target);
                continue;
            }

            Location warp = cpm.getWarp(target);
            if (warp != null) {
                player.teleport(warp);
                cpm.setLastCheckpoint(player.getUniqueId(), target);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "scoreboard players set " + player.getName() + " checkpoint " + target);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "execute at " + player.getName() + " run spawnpoint " + player.getName() + " ~ ~ ~");
                overlay.writeCheckpoint(target, cpm.getTotalCheckpoints());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0.8, 0), 20, 0.3, 0.8, 0.3);
                showTitle(player, "&eCP " + target, "&7Rolled back");
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cLost", "&7CP " + target + " has no save");
            }
        }
        return true;
    }

    // ─── /cp ────────────────────────────────────────────────────────────────

    public boolean cp() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!cpm.hasLastCheckpoint(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cNo CP", "&7No checkpoint saved");
                continue;
            }

            int current = cpm.getLastCheckpoint(player.getUniqueId());
            if (current <= 0) {
                org.bukkit.World world = Bukkit.getWorld("world");
                if (world == null) world = Bukkit.getWorlds().get(0);
                player.teleport(world.getSpawnLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
                showTitle(player, "&aSpawn", "&7Back to start");
                continue;
            }

            Location warp = cpm.getWarp(current);
            if (warp != null) {
                player.teleport(warp);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
                showTitle(player, "&aCP " + current, "&7Returned");
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cLost", "&7CP " + current + " has no save");
            }
        }
        return true;
    }

    // ─── /restart ───────────────────────────────────────────────────────────

    public boolean restart() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1f, 0.7f);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS, 30, 1));
            player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cpm.setLastCheckpoint(player.getUniqueId(), 0);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "scoreboard players set " + player.getName() + " checkpoint 0");
                overlay.writeCheckpoint(0, cpm.getTotalCheckpoints());

                org.bukkit.World world = Bukkit.getWorld("world");
                if (world == null) world = Bukkit.getWorlds().get(0);
                player.teleport(world.getSpawnLocation());
                plugin.getParkourEffects().giveEffects(player);

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 0.6f, 1.2f);
                player.spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1.5, 0), 80, 0.8, 1.5, 0.8);
                showTitle(player, "&8Reset", "&7Back to zero");
            }, 20L);
        }
        return true;
    }

    // ─── /exportcp ──────────────────────────────────────────────────────────

    public boolean exportCp(CommandSender sender) {
        try {
            File outFile = new File(plugin.getDataFolder(), "checkpoints_export.json");
            plugin.getDataFolder().mkdirs();

            JsonObject data = new JsonObject();
            data.addProperty("final", cpm.getFinalCheckpoint());

            JsonObject warpsJson = new JsonObject();
            for (Map.Entry<Integer, Location> entry : cpm.getWarps().entrySet()) {
                JsonObject w = new JsonObject();
                Location loc = entry.getValue();
                w.addProperty("x", loc.getX());
                w.addProperty("y", loc.getY());
                w.addProperty("z", loc.getZ());
                w.addProperty("yaw", loc.getYaw());
                w.addProperty("pitch", loc.getPitch());
                w.addProperty("world", loc.getWorld().getName());
                warpsJson.add(String.valueOf(entry.getKey()), w);
            }
            data.add("warps", warpsJson);

            JsonObject beaconsJson = new JsonObject();
            for (Map.Entry<String, Integer> entry : cpm.getBeacons().entrySet()) {
                beaconsJson.addProperty(entry.getKey(), entry.getValue());
            }
            data.add("beacons", beaconsJson);

            try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
                pw.println(data.toString());
            }

            sender.sendMessage("§a[Export] Saved to " + outFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            sender.sendMessage("§c[Export] Failed: " + e.getMessage());
            return true;
        }
    }
}
