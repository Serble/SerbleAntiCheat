package net.serble.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Config {
    private static FileConfiguration configuration;

    public static void load() {
        boolean ignored = Main.getInstance().getDataFolder().mkdirs();
        File file = new File(Main.getInstance().getDataFolder(), "config.yml");
        try {
            boolean ignored2 = file.createNewFile();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to create config.yml");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(Main.getInstance());
            return;
        }
        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to load config.yml");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(Main.getInstance());
            return;
        }
        AtomicBoolean changed = loadDefaults();
        if (!changed.get()) {
            return;
        }
        try {
            configuration.save(file);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to save config.yml");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    private static AtomicBoolean loadDefaults() {
        AtomicBoolean changed = new AtomicBoolean(false);
        checkOrSet(changed, "debug", true);
        checkOrSet(changed, "debug-check", "all");
        checkOrSet(changed, "lag-back", true);
        checkOrSet(changed, "tps-threshold", 19.0);
        checkOrSet(changed, "tps-sample-size", 100);
        checkOrSet(changed, "ping-threshold", 200);
        checkOrSet(changed, "disabled-worlds.worlds", new String[]{"example", "some-other-world"});
        checkOrSet(changed, "disabled-worlds.is-whitelist", false);
        checkOrSet(changed, "violation-score-threshold-pm", 50);

        // Checks Config Sections
        checkOrSet(changed, "checks.cps-cap", 20);

        // killaura
        checkOrSet(changed, "checks.killaura.enabled", true);
        checkOrSet(changed, "checks.killaura.entity-hit-cap", 10);
        checkOrSet(changed, "checks.killaura.sample-time", 500);
        checkOrSet(changed, "checks.killaura.violation-strength", 10);

        // nuker
        checkOrSet(changed, "checks.nuker.enabled", true);
        checkOrSet(changed, "checks.nuker.max-blocks-per-second", 25);
        checkOrSet(changed, "checks.nuker.sample-time", 1000);
        checkOrSet(changed, "checks.nuker.violation-strength", 10);

        // block-place-speed
        checkOrSet(changed, "checks.block-place-speed.enabled", true);
        checkOrSet(changed, "checks.block-place-speed.sample-time", 1000);
        checkOrSet(changed, "checks.block-place-speed.max-blocks-per-second", 15);
        checkOrSet(changed, "checks.block-place-speed.violation-strength", 20);

        // speed
        checkOrSet(changed, "checks.speed.enabled", true);
        checkOrSet(changed, "checks.speed.threshold", 0.2);
        checkOrSet(changed, "checks.speed.speed-average-time-period-ms", 1000);
        checkOrSet(changed, "checks.speed.sample-time", 1500);
        checkOrSet(changed, "checks.speed.violation-strength", 10);

        // jesus
        checkOrSet(changed, "checks.jesus.enabled", true);
        checkOrSet(changed, "checks.jesus.max-distance", 2.0);
        checkOrSet(changed, "checks.jesus.sample-time", 2);
        checkOrSet(changed, "checks.jesus.violation-strength", 10);

        // nofall
        checkOrSet(changed, "checks.nofall.enabled", true);
        checkOrSet(changed, "checks.nofall.enforce-fall-damage", true);
        checkOrSet(changed, "checks.nofall.time-until-fail", 1000);
        checkOrSet(changed, "checks.nofall.violation-strength", 100);

        // speed
        checkOrSet(changed, "checks.speed.enabled", true);
        checkOrSet(changed, "checks.speed.threshold", 1);

        // sneak
        checkOrSet(changed, "checks.sneak.enabled", true);
        checkOrSet(changed, "checks.sneak.violation-strength", 10);

        // unaided-levitation
        checkOrSet(changed, "checks.unaided-levitation.enabled", true);
        checkOrSet(changed, "checks.unaided-levitation.threshold", 3);
        checkOrSet(changed, "checks.unaided-levitation.violation-strength", 20);

        // basic-accelerate-up
        checkOrSet(changed, "checks.accelerate-up.enabled", true);
        checkOrSet(changed, "checks.accelerate-up.max-height-increase", 0.7);
        checkOrSet(changed, "checks.accelerate-up.violation-strength", 20);

        // flight
        checkOrSet(changed, "checks.flight.enabled", true);
        checkOrSet(changed, "checks.flight.violation-strength", 5);

        // no-view-limits
        checkOrSet(changed, "checks.no-view-limits.enabled", true);
        checkOrSet(changed, "checks.no-view-limits.violation-strength", 40);

        // inventory-walk
        checkOrSet(changed, "checks.inventory-walk.enabled", true);
        checkOrSet(changed, "checks.inventory-walk.lag-back", true);
        checkOrSet(changed, "checks.inventory-walk.close-inventory", false);
        checkOrSet(changed, "checks.inventory-walk.violation-strength", 10);

        // scaffold-walk
        checkOrSet(changed, "checks.scaffold-walk.enabled", true);
        checkOrSet(changed, "checks.scaffold-walk.max-ping", 120);
        checkOrSet(changed, "checks.scaffold-walk.violation-strength", 100);

        return changed;
    }

    private static void checkOrSet(AtomicBoolean changed, String key, Object value) {
        if (!configuration.isSet(key)) {
            if (value instanceof Map) {
                configuration.createSection(key, (Map<?, ?>) value);
            } else {
                configuration.set(key, value);
            }
            changed.set(true);
        }
    }

    public static FileConfiguration getConfiguration() {
        return configuration;
    }

    public static boolean shouldLagBack() {
        return configuration.getBoolean("lag-back");
    }

    public static boolean isCheckEnabled(String checkName) {
        return configuration.getBoolean("checks." + checkName + ".enabled");
    }

    public static boolean debug() {
        return configuration.getBoolean("debug");
    }

    public static String getDebugCheck() {
        return configuration.getString("debug-check");
    }

    public static boolean amIDebugging(Check check) {
        return debug() && (getDebugCheck().equalsIgnoreCase("all") || getDebugCheck().equalsIgnoreCase(check.getConfigName()));
    }
}
