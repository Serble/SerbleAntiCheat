package net.serble.anticheat;

import net.serble.anticheat.Checks.Combat.Killaura;
import net.serble.anticheat.Checks.Misc.*;
import net.serble.anticheat.Checks.Movement.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class ChecksManager {
    private final List<Check> checks = new ArrayList<>();

    public void registerChecks() {
        checks.add(new BasicAccelerateUp());
        checks.add(new UnaidedLevitation());
        checks.add(new BlockBreakSpeed());
        checks.add(new NoFall());
        checks.add(new BasicSpeed());
        checks.add(new Killaura());
        checks.add(new BlockPlaceSpeed());
        checks.add(new Sneak());
        checks.add(new Jesus());
        checks.add(new Flight());
        checks.add(new NoViewLimits());
        checks.add(new InventoryWalk());
        checks.add(new ScaffoldWalk());

        for (Check check : checks) {
            if (Config.isCheckEnabled(check.getConfigName())) {
                check.register();
            }
        }
    }

    public void register(Check check) {
        Bukkit.getLogger().info("Registered check " + check.getName() + ".");
    }

}
