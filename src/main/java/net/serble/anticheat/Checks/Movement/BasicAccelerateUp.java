package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class BasicAccelerateUp extends Check {

    @Override
    public String getName() {
        return "BasicAccelerateUp";
    }

    @Override
    public String getConfigName() {
        return "accelerate-up";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!canCheck(e.getPlayer())) return;

        if (isBypassSpeed(e.getPlayer())) {
            return;
        }

        if (Objects.requireNonNull(e.getTo()).getY() > e.getFrom().getY()) {
            if (e.getTo().getY() - e.getFrom().getY() > getConfig().getDouble("max-height-increase")) {
                failed(e.getPlayer());
                if (Config.shouldLagBack()) {
                    e.setCancelled(true);
                }
            }
        }
    }

}
