package com.parkourchube;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class ParkourEffects {

    private final ParkourCubePlugin plugin;
    private BukkitTask effectTask;

    public ParkourEffects(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        startEffectLoop();
    }

    public void giveEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0, false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 0, false, false, true));
        giveCompass(player);
    }

    public void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        Component name = Component.text("Checkpoint (Right Click)")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.BOLD, true);
        meta.displayName(name);
        meta.lore(List.of(
                Component.text("Right-click to return to your last checkpoint").color(NamedTextColor.GRAY)
        ));
        compass.setItemMeta(meta);

        // Check if player already has the compass
        boolean hasCompass = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()) {
                hasCompass = true;
                break;
            }
        }
        if (!hasCompass) {
            player.getInventory().setItem(8, compass);
        }
    }

    private void startEffectLoop() {
        effectTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 999999, 0, false, false, true));
                }
                if (!player.hasPotionEffect(PotionEffectType.SATURATION)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 999999, 0, false, false, true));
                }
                giveCompass(player);
            }
        }, 100L, 100L); // every 5 seconds
    }

    public void stop() {
        if (effectTask != null) effectTask.cancel();
    }
}
