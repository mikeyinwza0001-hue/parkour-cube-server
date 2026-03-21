package com.parkourchube;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.parkourchube.PlayerListener.showTitle;

public class FunCommands {

    private final ParkourCubePlugin plugin;
    private final CheckpointManager cpm;
    private final OverlayBridge overlay;

    // Fly time tracking
    private final ConcurrentHashMap<UUID, Integer> flyTime = new ConcurrentHashMap<>();

    public FunCommands(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        this.cpm = plugin.getCheckpointManager();
        this.overlay = plugin.getOverlayBridge();
        startFlyTimer();
    }

    // ─── /goto60 ────────────────────────────────────────────────────────────

    public boolean goto60() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location warp60 = cpm.getWarp(60);
            if (warp60 == null) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                showTitle(player, "&cNot Ready", "&7CP 60 not registered");
                continue;
            }

            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

            World world = Bukkit.getWorld("world");
            if (world == null) world = Bukkit.getWorlds().get(0);
            Location startLoc = new Location(world, 15.5, 70.4, -49.5);

            // Spawn dragon
            EnderDragon dragon = (EnderDragon) world.spawnEntity(startLoc, EntityType.ENDER_DRAGON);
            dragon.setSilent(true);
            dragon.setInvulnerable(true);
            dragon.setGravity(false);
            dragon.setAI(false);
            dragon.addScoreboardTag("cutscene_dragon");

            player.teleport(startLoc);
            dragon.addPassenger(player);

            showTitle(player, "&6&lFinal Stage", "");

            // Waypoints for dragon flight path
            double[][] waypoints = {
                { 15.500, 70.400, -49.500 },   // start
                { -26.855, 98.929, -53.305 },
                { -77.532, 150.657, 22.046 },
                { 16.752, 208.776, 108.568 },
                { 69.495, 256.781, 23.032 },
                { 11.586, 280.395, -28.014 },
                { 11.475, 259.000, 16.464 },    // end (dive down)
            };
            int ticksPerSegment = 30;

            final Player fp = player;
            final World fw = world;

            new BukkitRunnable() {
                int segment = 0;
                int tick = 0;

                @Override
                public void run() {
                    if (!fp.isOnline() || dragon.isDead()) {
                        cleanup();
                        cancel();
                        return;
                    }

                    if (segment >= waypoints.length - 1) {
                        // Finish — arrived at final waypoint
                        fp.playSound(fp.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        fp.spawnParticle(Particle.PORTAL, fp.getLocation(), 200, 2, 2, 2);
                        fp.leaveVehicle();
                        cleanup();

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            fp.teleport(warp60);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                fp.teleport(warp60);
                                fp.spawnParticle(Particle.REVERSE_PORTAL, fp.getLocation().add(0, 1, 0), 120, 0, 1, 0);
                                fp.playSound(fp.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                showTitle(fp, "&6&lTop Reached!", "&aCP 60 - Final");

                                cpm.setLastCheckpoint(fp.getUniqueId(), 60);
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        "scoreboard players set " + fp.getName() + " checkpoint 60");
                                overlay.writeCheckpoint(60, cpm.getTotalCheckpoints());
                            }, 5L);
                        }, 2L);

                        cancel();
                        return;
                    }

                    double t = (double) tick / ticksPerSegment;
                    double[] from = waypoints[segment];
                    double[] to = waypoints[segment + 1];
                    double cx = from[0] + (to[0] - from[0]) * t;
                    double cy = from[1] + (to[1] - from[1]) * t;
                    double cz = from[2] + (to[2] - from[2]) * t;

                    // Calculate yaw to face direction of travel
                    double dx = to[0] - from[0];
                    double dz = to[2] - from[2];
                    float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                    // Calculate pitch: negative = looking up, positive = looking down
                    double dy = to[1] - from[1];
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

                    Location loc = new Location(fw, cx, cy, cz, yaw, pitch);
                    dragon.teleport(loc);

                    // Particles based on flight phase
                    if (segment < waypoints.length - 2) {
                        // Climbing / cruising
                        if (tick % 3 == 0) fp.playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1f);
                        fw.spawnParticle(Particle.FLAME, loc, 5, 1, 0.3, 1);
                        fw.spawnParticle(Particle.CLOUD, loc, 3, 0.5, 0.2, 0.5);
                    } else {
                        // Diving down (last segment)
                        fw.spawnParticle(Particle.PORTAL, loc, 20, 1, 1, 1);
                    }

                    tick++;
                    if (tick > ticksPerSegment) {
                        tick = 0;
                        segment++;
                    }
                }

                void cleanup() {
                    dragon.remove();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "kill @e[type=ender_dragon,tag=cutscene_dragon]");
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        return true;
    }

    // ─── /trapweb ───────────────────────────────────────────────────────────

    public boolean trapWeb() {
        List<Location> webLocs = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location foot = player.getLocation();
            Location body = foot.clone().add(0, 1, 0);

            if (body.getBlock().getType() == Material.AIR) {
                body.getBlock().setType(Material.COBWEB);
                webLocs.add(body);
            } else if (foot.getBlock().getType() == Material.AIR) {
                foot.getBlock().setType(Material.COBWEB);
                webLocs.add(foot);
            } else {
                Random rand = new Random();
                for (int i = 0; i < 20; i++) {
                    int rx = rand.nextInt(5) - 2;
                    int rz = rand.nextInt(5) - 2;
                    Location tryLoc = foot.clone().add(rx, 1, rz);
                    if (tryLoc.getBlock().getType() == Material.AIR) {
                        tryLoc.getBlock().setType(Material.COBWEB);
                        webLocs.add(tryLoc);
                        break;
                    }
                }
            }
            player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1f, 1.3f);
            player.spawnParticle(Particle.WHITE_ASH, player.getLocation().add(0, 0.5, 0), 20, 0.3, 0.5, 0.3);
            player.sendActionBar(PlayerListener.colorize("&7Caught!"));
        }

        // Remove webs after 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Location loc : webLocs) {
                if (loc.getBlock().getType() == Material.COBWEB) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
        }, 100L);
        return true;
    }

    // ─── /fly <seconds> ─────────────────────────────────────────────────────

    public boolean fly(String[] args) {
        if (args.length < 1) return false;
        int seconds;
        try { seconds = Integer.parseInt(args[0]); } catch (NumberFormatException e) { return false; }

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (flyTime.containsKey(uuid) && flyTime.get(uuid) > 0) {
                flyTime.merge(uuid, seconds, Integer::sum);
                player.sendActionBar(PlayerListener.colorize(
                        "&b+" + seconds + "s &7(" + flyTime.get(uuid) + "s left)"));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            } else {
                flyTime.put(uuid, seconds);
                player.setAllowFlight(true);
                player.setFlying(true);
                player.sendActionBar(PlayerListener.colorize("&bFly &7" + seconds + "s"));
                player.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            }
        }
        return true;
    }

    private void startFlyTimer() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<UUID, Integer>> it = flyTime.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> entry = it.next();
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) {
                    it.remove();
                    continue;
                }
                int remaining = entry.getValue() - 1;
                if (remaining <= 0) {
                    it.remove();
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    showTitle(player, "&8Landing", "&7Fly time ended");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                } else {
                    entry.setValue(remaining);
                    showTitle(player, "&b~", "&7" + remaining + "s");
                    player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                }
            }
        }, 20L, 20L); // every second
    }

    // ─── /tntz <amount> ─────────────────────────────────────────────────────

    public boolean tntz(CommandSender sender, String[] args) {
        if (args.length < 1) return false;
        int amount;
        try { amount = Integer.parseInt(args[0]); } catch (NumberFormatException e) { return false; }
        if (amount < 1) {
            sender.sendMessage("§cAmount must be greater than 0");
            return true;
        }

        Player target = null;
        for (Player p : Bukkit.getOnlinePlayers()) { target = p; }
        if (target == null) return true;

        final Player ft = target;
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= amount || !ft.isOnline()) { cancel(); return; }

                Location loc = ft.getLocation().add(0, 2, 0);
                Random rand = new Random();
                // Increase horizontal spread (vx, vz)
                double vx = rand.nextDouble() * 4.0 - 2.0;
                // Decrease vertical boost (vy)
                double vy = rand.nextDouble() * 0.5 + 0.3;
                double vz = rand.nextDouble() * 4.0 - 2.0;

                for (Player nearby : ft.getWorld().getPlayers()) {
                    if (nearby.getLocation().distance(loc) <= 15) {
                        nearby.setVelocity(new Vector(vx, vy, vz).multiply(1.5));
                    }
                }

                ft.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
                for (Player nearby : ft.getWorld().getPlayers()) {
                    if (nearby.getLocation().distance(loc) <= 30) {
                        nearby.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 2f);
                    }
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
        return true;
    }

    // ─── /chickenride ───────────────────────────────────────────────────────

    public boolean chickenRide() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int nextCp = cpm.getLastCheckpoint(player.getUniqueId()) + 1;
            Location targetLoc = cpm.getWarp(nextCp);
            if (targetLoc == null) {
                player.sendActionBar(PlayerListener.colorize("&cNext checkpoint not registered yet!"));
                continue;
            }

            Location startLoc = player.getLocation().clone();
            double xDiff = targetLoc.getX() - startLoc.getX();
            double yDiff = targetLoc.getY() - startLoc.getY();
            double zDiff = targetLoc.getZ() - startLoc.getZ();

            int steps = 60;
            double xStep = xDiff / steps;
            double yStep = yDiff / steps;
            double zStep = zDiff / steps;

            Chicken chicken = (Chicken) player.getWorld().spawnEntity(startLoc, EntityType.CHICKEN);
            chicken.setSilent(true);
            chicken.setInvulnerable(true);
            chicken.setGravity(false);
            chicken.addScoreboardTag("cutscene_chicken");

            chicken.addPassenger(player);
            showTitle(player, "&e&lChicken Ride", "&7To CP " + nextCp);

            final Player fp = player;
            final int fnext = nextCp;
            new BukkitRunnable() {
                int step = 0;
                double cx = startLoc.getX(), cy = startLoc.getY(), cz = startLoc.getZ();

                @Override
                public void run() {
                    if (!fp.isOnline() || chicken.isDead()) {
                        chicken.remove();
                        cancel();
                        return;
                    }
                    if (step >= steps) {
                        fp.playSound(fp.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                        fp.leaveVehicle();
                        chicken.remove();
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                "kill @e[type=chicken,tag=cutscene_chicken]");

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            fp.teleport(targetLoc);
                            fp.playSound(fp.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            cpm.setLastCheckpoint(fp.getUniqueId(), fnext);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    "scoreboard players set " + fp.getName() + " checkpoint " + fnext);
                            overlay.writeCheckpoint(fnext, cpm.getTotalCheckpoints());
                        }, 2L);

                        cancel();
                        return;
                    }

                    cx += xStep; cy += yStep; cz += zStep;
                    Location loc = new Location(fp.getWorld(), cx, cy, cz, 0, 0);
                    chicken.teleport(loc);
                    fp.playSound(loc, Sound.ENTITY_CHICKEN_AMBIENT, 0.5f, 1f);
                    fp.getWorld().spawnParticle(Particle.CLOUD, loc, 10, 0.5, 0.5, 0.5);
                    step++;
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
        return true;
    }
}
