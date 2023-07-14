package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Schemas.Tuple;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Flight extends Check {
    private final HashMap<UUID, List<Tuple<Long, Location>>> playerDetails = new HashMap<>();

    @Override
    public String getName() {
        return "Flight";
    }

    @Override
    public String getConfigName() {
        return "flight";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (isBypassFly(p)) {
            return;
        }

        Location from = e.getFrom();
        Long fromTime = System.currentTimeMillis();
        if (playerDetails.containsKey(p.getUniqueId())) {
            playerDetails.get(p.getUniqueId()).removeIf(tuple -> tuple.getFirst() < System.currentTimeMillis() - 1000);
            from = playerDetails.get(p.getUniqueId()).get(0).getSecond();
            fromTime = playerDetails.get(p.getUniqueId()).get(0).getFirst();
        }
        Location to = e.getTo();

        assert to != null;
        if (!Objects.requireNonNull(from.getWorld()).getName().equals(Objects.requireNonNull(to.getWorld()).getName())) {
            playerDetails.remove(p.getUniqueId());
            return;
        }

        if (Objects.requireNonNull(to).getY() != from.getY()) {
            playerDetails.remove(p.getUniqueId());
            return;
        }

        // Make sure they moved on the x or z axis.
        if (to.getX() == from.getX() && to.getZ() == from.getZ()) {
            return;
        }

        if (isOnGround(p)) {
            playerDetails.remove(p.getUniqueId());
            return;
        }

        // Return if the oldest location is newer than half a second
        if (fromTime > System.currentTimeMillis() - 500) {
            return;
        }

        failed(p);  // They have moved without going up or down, and they are not on the ground.
    }
}
