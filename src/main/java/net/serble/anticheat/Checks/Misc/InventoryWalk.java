package net.serble.anticheat.Checks.Misc;

import net.serble.anticheat.Check;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class InventoryWalk extends Check {
    private final Map<Player, Boolean> playerInventoryStatus = new HashMap<>();

    @Override
    public String getName() {
        return "InventoryWalk";
    }

    @Override
    public String getConfigName() {
        return "inventory-walk";
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            playerInventoryStatus.put((Player)humanEntity, true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player){
            playerInventoryStatus.put((Player)humanEntity, false);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (!playerInventoryStatus.getOrDefault(p, false)) {
            return;
        }

        failed(p);
        if (getConfig().getBoolean("lag-back")) {
            e.setCancelled(true);
        }
        if (getConfig().getBoolean("close-inventory")) {
            p.closeInventory();
        }
    }
}
