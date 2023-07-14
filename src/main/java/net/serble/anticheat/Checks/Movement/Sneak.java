package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class Sneak extends Check {
    @Override
    public String getName() {
        return "Sneak";
    }

    @Override
    public String getConfigName() {
        return "sneak";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
//        if (!canCheck(e.getPlayer())) return;
//
//        if (!e.getPlayer().isSneaking() || !e.getPlayer().isSprinting()) {
//            return;
//        }
//
//        failed(e.getPlayer());
//        if (Config.shouldLagBack()) {
//            e.setCancelled(true);
//            e.getPlayer().setSprinting(false);
//        }
    }
}
