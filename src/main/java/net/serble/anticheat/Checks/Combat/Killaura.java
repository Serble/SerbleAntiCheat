package net.serble.anticheat.Checks.Combat;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Killaura extends Check {
    private final HashMap<UUID, List<Long>> hits = new HashMap<>();

    @Override
    public String getName() {
        return "Killaura";
    }

    @Override
    public String getConfigName() {
        return "killaura";
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        Player p = (Player) e.getDamager();
        if (!canCheck(p)) return;

        List<Long> hits = this.hits.get(p.getUniqueId());
        if (hits == null) {
            hits = new java.util.ArrayList<>();
        }

        int sampleTime = getConfig().getInt("sample-time");

        hits.add(System.currentTimeMillis());
        hits.removeIf(l -> System.currentTimeMillis() - l > sampleTime);
        this.hits.put(p.getUniqueId(), hits);

        int hitsPerSecond = hits.size() * (1000 / sampleTime);

        if (hitsPerSecond > getConfig().getInt("entity-hit-cap")) {
            failed(p);
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }
    }

}
