package net.serble.anticheat.Checks.Misc;

import net.serble.anticheat.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoViewLimits extends Check {
    @Override
    public String getName() {
        return "NoViewLimits";
    }

    @Override
    public String getConfigName() {
        return "no-view-limits";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (!canCheck(p)) return;

        float pitch = p.getLocation().getPitch();

        if (pitch > 90.01D || pitch < -90.01D) {
            failed(p);  // Player head is beyond the normal view limits.
        }
    }
}
