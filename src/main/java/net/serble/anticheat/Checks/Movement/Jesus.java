package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.Objects;

public class Jesus extends Check {
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
        if (!canCheck(p)) return;

        if (p.isSwimming()) {
            return;
        }

        if (isBypassFly(p)) {
            return;
        }

        List<Material> standingOn = getBlocksPlayerIsStandingOn(p);

        if (standingOn.stream().anyMatch(m -> m != Material.WATER && m != Material.LAVA)) {
            return;
        }

        // Did they move on the y-axis?
        if (Objects.requireNonNull(e.getTo()).getY() != e.getFrom().getY()) {
            return;
        }

        if (getBlocksPlayerIsStandingOnAndAbove(p, 3, true).stream().anyMatch(m -> m == Material.WATER || m == Material.LAVA)) {
            // They could be in a liquid and not standing on it
            return;
        }

        // They moved, but they didn't move on the y-axis, so they must be jesus.
        failed(p);
    }
}
