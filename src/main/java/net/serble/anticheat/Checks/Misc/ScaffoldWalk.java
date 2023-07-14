package net.serble.anticheat.Checks.Misc;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class ScaffoldWalk extends Check {
    @Override
    public String getName() {
        return "ScaffoldWalk";
    }

    @Override
    public String getConfigName() {
        return "scaffold-walk";
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (p.getPing() > getConfig().getInt("max-ping")) {
            return;
        }

        Material heldItem = p.getInventory().getItemInMainHand().getType();
        Material offhandItem = p.getInventory().getItemInOffHand().getType();
        Material placedBlock = p.getLocation().getBlock().getType();

        if ((heldItem != placedBlock) || (offhandItem != placedBlock)) {
            return;
        }

        failed(e.getPlayer());  // They placed a block without holding it
        if (Config.shouldLagBack()) {
            e.setCancelled(true);
        }
    }
}
