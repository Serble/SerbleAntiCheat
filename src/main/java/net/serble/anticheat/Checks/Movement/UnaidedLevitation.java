package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class UnaidedLevitation extends Check {

    private final HashMap<UUID, Double> blocksRaised = new HashMap<>();

    @Override
    public String getName() {
        return "UnaidedLevitation";
    }

    @Override
    public String getConfigName() {
        return "unaided-levitation";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;
        UUID playerId = p.getUniqueId();

        if (isBypassFly(p)) {
            return;
        }

        if (isOnGround(p)) {
            blocksRaised.remove(playerId);
            return;
        }

        if (e.getFrom().getY() > Objects.requireNonNull(e.getTo()).getY()) {
            return;
        }

        // add the distance between the two y values to the hashmap
        blocksRaised.put(playerId, blocksRaised.getOrDefault(playerId, 0.0) + (e.getTo().getY() - e.getFrom().getY()));

        // check if the player has gone up more than 3 blocks without going down then fail
        if (blocksRaised.get(playerId) > getConfig().getDouble("threshold")) {
            failed(p);
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }

    }

}
