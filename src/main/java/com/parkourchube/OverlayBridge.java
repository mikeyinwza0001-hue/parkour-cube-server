package com.parkourchube;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OverlayBridge {

    private final ParkourCubePlugin plugin;
    private final File logFile;

    public OverlayBridge(ParkourCubePlugin plugin) {
        this.plugin = plugin;
        String logPath = plugin.getConfig().getString("overlay.log-file", "plugins/Skript/logs/cp_overlay_data.log");
        this.logFile = new File(plugin.getServer().getWorldContainer(), logPath);
        logFile.getParentFile().mkdirs();
    }

    public void writeCheckpoint(int current, int max) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
            pw.println(current + "/" + max);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write overlay data: " + e.getMessage());
        }
    }
}
