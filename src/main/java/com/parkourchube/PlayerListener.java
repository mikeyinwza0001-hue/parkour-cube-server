package com.parkourchube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final ParkourCubePlugin plugin;
    private final CheckpointManager cpm;
    private final OverlayBridge overlay;

    // Players currently being warped from lava (debounce)
    private final Set<UUID> lavaWarping = new HashSet<>();
    // Players in final CP countdown
    private final Set<UUID> finalCounting = new HashSet<>();

    public PlayerListener(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.cpm = plugin.getCheckpointManager();
        this.overlay = plugin.getOverlayBridge();
    }

    // ─── JOIN ───────────────────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "scoreboard players set " + player.getName() + " checkpoint 0");
            cpm.setLastCheckpoint(player.getUniqueId(), 0);
            plugin.getParkourEffects().giveEffects(player);
        }, 5L);

        // Track player join to mabel-tracker
        if (plugin.getSecurityManager() != null) {
            plugin.getSecurityManager().sendPlayerEvent("join", player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Track player quit to mabel-tracker
        if (plugin.getSecurityManager() != null) {
            plugin.getSecurityManager().sendPlayerEvent("quit", event.getPlayer());
        }
    }

    // ─── COMPASS RIGHT CLICK ────────────────────────────────────────────────

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!event.getAction().isRightClick()) return;
        if (player.getInventory().getItemInMainHand().getType() != Material.COMPASS) return;
        if (!player.getInventory().getItemInMainHand().hasItemMeta()) return;
        if (!player.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) return;

        event.setCancelled(true);

        int lastCp = cpm.getLastCheckpoint(player.getUniqueId());
        if (lastCp <= 0) {
            if (!cpm.hasLastCheckpoint(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                showTitle(player, "&cNo Checkpoint", "&7Step on a beacon first");
                return;
            }
            teleportToSpawn(player);
            return;
        }

        Location warp = cpm.getWarp(lastCp);
        if (warp != null) {
            player.teleport(warp);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            showTitle(player, "&cLost", "&7Checkpoint " + lastCp + " has no save");
        }
    }

    // ─── DAMAGE (VOID + OTHER) ──────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            player.setFireTicks(0);
            teleportToCheckpoint(player);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        } else if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            event.setDamage(0);
        }
    }

    // ─── PLAYER MOVE (LAVA + BEACON) ────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        // Only check when block position changes
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // --- Lava check ---
        handleLava(player);

        // --- Beacon check ---
        handleBeacon(player);
    }

    private void handleLava(Player player) {
        if (lavaWarping.contains(player.getUniqueId())) return;

        Block atPlayer = player.getLocation().getBlock();
        if (atPlayer.getType() != Material.LAVA) return;

        // Check waterfall (lava above + lava at player)
        Block above = atPlayer.getRelative(0, 1, 0);
        if (above.getType() == Material.LAVA) {
            // Waterfall - check exemptions
            if (player.getInventory().getItemInMainHand().getType() == Material.WARPED_FUNGUS_ON_A_STICK) return;
            if (player.getVehicle() != null && player.getLocation().subtract(0, 1, 0).getBlock().getType() == Material.LAVA) return;
        }

        lavaWarping.add(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1f, 1f);
        player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "particle minecraft:flame " + player.getName() + " 0.3 0.3 0.3 0.1 30 force " + player.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setFireTicks(0);
            teleportToCheckpoint(player);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
            lavaWarping.remove(player.getUniqueId());
        }, 10L);
    }

    private void handleBeacon(Player player) {
        Block below = player.getLocation().subtract(0, 1, 0).getBlock();
        if (below.getType() != Material.BEACON) return;

        Integer cpNum = cpm.getBeaconCheckpoint(below.getLocation());
        if (cpNum == null) return;

        // ─── Final checkpoint cinematic ───
        if (cpNum == cpm.getFinalCheckpoint()) {
            handleFinalCheckpoint(player);
            return;
        }

        // ─── Normal checkpoint ───
        int previous = cpm.getLastCheckpoint(player.getUniqueId());
        if (cpNum == previous) return;

        cpm.setLastCheckpoint(player.getUniqueId(), cpNum);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "scoreboard players set " + player.getName() + " checkpoint " + cpNum);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "execute at " + player.getName() + " run spawnpoint " + player.getName() + " ~ ~ ~");
        overlay.writeCheckpoint(cpNum, cpm.getTotalCheckpoints());

        if (cpNum > previous) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 40, 0.5, 1, 0.5);
            showTitle(player, "&aCP " + cpNum, "&7Keep going!");
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 0.8f);
            player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 0.8, 0), 20, 0.3, 0.8, 0.3);
            showTitle(player, "&eCP " + cpNum, "&7Back to checkpoint");
        }
    }

    private void handleFinalCheckpoint(Player player) {
        if (finalCounting.contains(player.getUniqueId())) return;
        finalCounting.add(player.getUniqueId());

        // 10-second countdown
        new Object() {
            int count = 10;
            void tick() {
                if (!finalCounting.contains(player.getUniqueId())) return;
                Block below = player.getLocation().subtract(0, 1, 0).getBlock();
                if (below.getType() != Material.BEACON) {
                    finalCounting.remove(player.getUniqueId());
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    showTitle(player, "&cCancelled", "");
                    return;
                }

                count--;
                int left = count;

                if (left <= 0) {
                    // WIN
                    finalCounting.remove(player.getUniqueId());
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
                    player.spawnParticle(Particle.FIREWORK, player.getLocation().add(0, 1, 0), 200, 0.5, 0.5, 0.5, 0.01);
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    showTitle(player, "&6&lYOU WIN!", "");

                    cpm.setLastCheckpoint(player.getUniqueId(), 0);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "scoreboard players set " + player.getName() + " checkpoint 0");
                    overlay.writeCheckpoint(0, cpm.getTotalCheckpoints());

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        teleportToSpawn(player);
                    }, 40L);
                    return;
                }

                showTitle(player, "&c&l" + left, "&7Almost there...");

                if (left > 3) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                    Bukkit.getScheduler().runTaskLater(plugin, this::tick, 20L);
                } else if (left == 3) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 1f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20, 0));
                    Bukkit.getScheduler().runTaskLater(plugin, this::tick, 24L);
                } else if (left == 2) {
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1f, 1f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20, 0));
                    Bukkit.getScheduler().runTaskLater(plugin, this::tick, 24L);
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 3));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 20, 0));
                    Bukkit.getScheduler().runTaskLater(plugin, this::tick, 40L);
                }
            }
        }.tick();
    }

    // ─── DEATH ──────────────────────────────────────────────────────────────

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int lastCp = cpm.getLastCheckpoint(player.getUniqueId());
            if (lastCp > 0) {
                Location warp = cpm.getWarp(lastCp);
                if (warp != null) {
                    player.teleport(warp);
                }
            }
        }, 2L);
    }

    // ─── UTILITIES ──────────────────────────────────────────────────────────

    public void teleportToCheckpoint(Player player) {
        int lastCp = cpm.getLastCheckpoint(player.getUniqueId());
        if (lastCp > 0) {
            Location warp = cpm.getWarp(lastCp);
            if (warp != null) {
                player.teleport(warp);
                return;
            }
        }
        teleportToSpawn(player);
    }

    public void teleportToSpawn(Player player) {
        World world = Bukkit.getWorld("world");
        if (world == null) world = Bukkit.getWorlds().get(0);
        player.teleport(world.getSpawnLocation());
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 1, 0.5);
    }

    public static void showTitle(Player player, String title, String subtitle) {
        Component titleComp = colorize(title);
        Component subtitleComp = colorize(subtitle);
        player.showTitle(Title.title(titleComp, subtitleComp,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(1000), Duration.ZERO)));
    }

    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();
        // Simple & color code parser
        Component result = Component.empty();
        NamedTextColor currentColor = NamedTextColor.WHITE;
        boolean bold = false;
        boolean italic = false;

        StringBuilder segment = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                // Flush current segment
                if (!segment.isEmpty()) {
                    Component part = Component.text(segment.toString()).color(currentColor);
                    if (bold) part = part.decoration(TextDecoration.BOLD, true);
                    if (italic) part = part.decoration(TextDecoration.ITALIC, true);
                    result = result.append(part);
                    segment = new StringBuilder();
                }
                char code = text.charAt(i + 1);
                switch (code) {
                    case '0' -> currentColor = NamedTextColor.BLACK;
                    case '1' -> currentColor = NamedTextColor.DARK_BLUE;
                    case '2' -> currentColor = NamedTextColor.DARK_GREEN;
                    case '3' -> currentColor = NamedTextColor.DARK_AQUA;
                    case '4' -> currentColor = NamedTextColor.DARK_RED;
                    case '5' -> currentColor = NamedTextColor.DARK_PURPLE;
                    case '6' -> currentColor = NamedTextColor.GOLD;
                    case '7' -> currentColor = NamedTextColor.GRAY;
                    case '8' -> currentColor = NamedTextColor.DARK_GRAY;
                    case '9' -> currentColor = NamedTextColor.BLUE;
                    case 'a' -> currentColor = NamedTextColor.GREEN;
                    case 'b' -> currentColor = NamedTextColor.AQUA;
                    case 'c' -> currentColor = NamedTextColor.RED;
                    case 'd' -> currentColor = NamedTextColor.LIGHT_PURPLE;
                    case 'e' -> currentColor = NamedTextColor.YELLOW;
                    case 'f' -> currentColor = NamedTextColor.WHITE;
                    case 'l' -> bold = true;
                    case 'o' -> italic = true;
                    case 'r' -> { currentColor = NamedTextColor.WHITE; bold = false; italic = false; }
                }
                i++; // skip code char
            } else {
                segment.append(text.charAt(i));
            }
        }
        if (!segment.isEmpty()) {
            Component part = Component.text(segment.toString()).color(currentColor);
            if (bold) part = part.decoration(TextDecoration.BOLD, true);
            if (italic) part = part.decoration(TextDecoration.ITALIC, true);
            result = result.append(part);
        }
        return result;
    }
}
