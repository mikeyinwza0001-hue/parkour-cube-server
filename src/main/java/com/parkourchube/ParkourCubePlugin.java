package com.parkourchube;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ParkourCubePlugin extends JavaPlugin {

    private static ParkourCubePlugin instance;
    private SecurityManager securityManager;
    private CheckpointManager checkpointManager;
    private OverlayBridge overlayBridge;
    private ParkourEffects parkourEffects;
    private boolean securityApproved = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize overlay bridge
        overlayBridge = new OverlayBridge(this);

        // Initialize checkpoint manager
        checkpointManager = new CheckpointManager(this);

        // Initialize security manager and verify
        securityManager = new SecurityManager(this);
        securityManager.verify(approved -> {
            if (!approved) {
                getLogger().severe("========================================");
                getLogger().severe(" ParkourCube: NOT APPROVED");
                getLogger().severe(" Please contact admin for approval.");
                getLogger().severe("========================================");
                Bukkit.getScheduler().runTask(this, () -> {
                    Bukkit.getPluginManager().disablePlugin(this);
                });
                return;
            }

            securityApproved = true;
            getLogger().info("Security check passed. Loading plugin...");

            // Load checkpoint data from file first, then fallback to API
            checkpointManager.loadFromFile();
            checkpointManager.loadFromApi(); // API will update if available

            // Register listeners and commands
            Bukkit.getScheduler().runTask(this, () -> {
                parkourEffects = new ParkourEffects(this);

                PlayerListener listener = new PlayerListener(this);
                Bukkit.getPluginManager().registerEvents(listener, this);

                CheckpointCommands cpCmds = new CheckpointCommands(this);
                FunCommands funCmds = new FunCommands(this);
                MbpkCommand mbpkCmd = new MbpkCommand(cpCmds, funCmds);
                getCommand("mbpk").setExecutor(mbpkCmd);
                getCommand("mbpk").setTabCompleter(mbpkCmd);

                // Setup scoreboard
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "scoreboard objectives add checkpoint dummy Checkpoint");

                // Start heartbeat
                securityManager.startHeartbeat();

                getLogger().info("ParkourCube v" + getDescription().getVersion() + " enabled!");
            });
        });
    }

    @Override
    public void onDisable() {
        if (securityManager != null) {
            securityManager.shutdown();
        }
        getLogger().info("ParkourCube disabled.");
    }

    public static ParkourCubePlugin getInstance() { return instance; }
    public SecurityManager getSecurityManager() { return securityManager; }
    public CheckpointManager getCheckpointManager() { return checkpointManager; }
    public OverlayBridge getOverlayBridge() { return overlayBridge; }
    public ParkourEffects getParkourEffects() { return parkourEffects; }
    public boolean isSecurityApproved() { return securityApproved; }
}
