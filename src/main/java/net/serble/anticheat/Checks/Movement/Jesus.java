package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Jesus extends Check {
    private final HashMap<UUID, Float> jesusDistances = new HashMap<>();

    @Override
    public String getName() {
        return "Jesus";
    }

    @Override
    public String getConfigName() {
        return "jesus";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) {
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        if (p.isSwimming()) {
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        if (isBypassFly(p)) {
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        List<Material> standingOn = getBlocksPlayerIsStandingOn(p);

        if (standingOn.stream().anyMatch(m -> m != Material.WATER && m != Material.LAVA)) {
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        // Did they move on the y-axis?
        if (Objects.requireNonNull(e.getTo()).getY() != e.getFrom().getY()) {
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        if (getBlocksPlayerIsStandingOnAndAbove(p, 3, true).stream().anyMatch(m -> m == Material.WATER || m == Material.LAVA)) {
            // They could be in a liquid and not standing on it
            jesusDistances.remove(p.getUniqueId());
            return;
        }

        // They moved, but they didn't move on the y-axis, so they must be jesus, right?
        float distance = (float) e.getFrom().distance(e.getTo());
        jesusDistances.put(p.getUniqueId(), jesusDistances.getOrDefault(p.getUniqueId(), 0.0f) + distance);

        // Check if they moved more than the threshold
        if (jesusDistances.get(p.getUniqueId()) > getConfig().getDouble("max-distance")) {
            failed(p);
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }
    }
}
