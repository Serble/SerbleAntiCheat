package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
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
            debug(p, "bypassed");
            return;
        }

        Location from = e.getFrom();
        Long fromTime = System.currentTimeMillis();
        Location to = e.getTo();
        if (playerDetails.containsKey(p.getUniqueId())) {
            playerDetails.get(p.getUniqueId()).removeIf(tuple -> tuple.getFirst() < System.currentTimeMillis() - 1000);
            playerDetails.get(p.getUniqueId()).add(new Tuple<>(System.currentTimeMillis(), e.getTo()));

            if (playerDetails.get(p.getUniqueId()).size() == 1) {
                debug(p, "not enough samples");
                return;
            }
            from = playerDetails.get(p.getUniqueId()).get(0).getSecond();
            fromTime = playerDetails.get(p.getUniqueId()).get(0).getFirst();
        } else {
            List<Tuple<Long, Location>> details = new java.util.ArrayList<>();
            details.add(new Tuple<>(System.currentTimeMillis(), e.getTo()));
            playerDetails.put(p.getUniqueId(), details);
        }

        assert to != null;
        if (!Objects.requireNonNull(from.getWorld()).getName().equals(Objects.requireNonNull(to.getWorld()).getName())) {
            playerDetails.remove(p.getUniqueId());
            debug(p, "changed world");
            return;
        }

        if (Objects.requireNonNull(to).getY() != from.getY()) {
            playerDetails.remove(p.getUniqueId());
            debug(p, "changed y");
            return;
        }

        // Make sure they moved on the x or z axis.
        if (to.getX() == from.getX() && to.getZ() == from.getZ()) {
            debug(p, "didn't move");
            return;
        }

        if (isOnGround(p)) {
            playerDetails.remove(p.getUniqueId());
            debug(p, "on ground");
            return;
        }

        // Return if the oldest location is newer than half a second
        if (fromTime > System.currentTimeMillis() - 500) {
            debug(p, "sample time not met");
            return;
        }

        failed(p);  // They have moved without going up or down, and they are not on the ground.
        if (Config.shouldLagBack()) {
            e.setCancelled(true);
        }
    }
}
