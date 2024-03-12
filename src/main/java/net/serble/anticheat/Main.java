package net.serble.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private ChecksManager checksManager;
    private ViolationsManager violationsManager;

    public static Main getInstance() {
        return instance;
    }

    public ChecksManager getChecksManager() {
        return checksManager;
    }

    public ViolationsManager getViolationsManager() {
        return violationsManager;
    }

    @Override
    public void onEnable() {
        instance = this;

        Config.load();

        violationsManager = new ViolationsManager();

        checksManager = new ChecksManager();
        checksManager.registerChecks();

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new TpsTracker(), 100L, 1L); // Start TPS Tracker
        getLogger().info("Serble AntiCheat has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Serble AntiCheat has been disabled!");
    }

}