package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import net.serble.anticheat.Schemas.Tuple;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BasicSpeed extends Check {
    private final HashMap<UUID, List<Tuple<Long, Location>>> playerDetails = new HashMap<>();

    @Override
    public String getName() {
        return "BasicSpeed";
    }

    @Override
    public String getConfigName() {
        return "speed";
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();

        if (isBypassSpeed(p)) {
            return;
        }

        playerDetails.remove(p.getUniqueId());
        debug(p, "teleported");
    }

    @EventHandler
    public void onMove2(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (isBypassSpeed(p)) {
            debug(p, "bypassed");
            return;
        }

        List<Tuple<Long, Location>> details = playerDetails.get(p.getUniqueId());
        if (details == null) {
            details = new java.util.ArrayList<>();
            playerDetails.put(p.getUniqueId(), details);
            debug(p, "created new details");
            return;
        }

        details.removeIf(tuple -> tuple.getFirst() < System.currentTimeMillis() - getConfig().getInt("sample-time"));
        details.add(new Tuple<>(System.currentTimeMillis(), e.getTo()));
        playerDetails.put(p.getUniqueId(), details);

        Location from = details.get(0).getSecond();
        long timeSinceFrom = System.currentTimeMillis() - details.get(0).getFirst();
        Location to = e.getTo();

        int averagingPeriod = getConfig().getInt("speed-average-time-period-ms");
        if (timeSinceFrom < averagingPeriod) {
            debug(p, "averaging time not met");
            return;  // Average must be over the averaging period.
        }

        assert to != null;
        double horizontalDistance = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
        double speed = horizontalDistance / (timeSinceFrom / (double) averagingPeriod);
        double expectedMaxWalkSpeed = 4.317 * (p.getWalkSpeed() / 0.2);
        expectedMaxWalkSpeed = expectedMaxWalkSpeed * 1.3 * 1.5;

        if (speed > expectedMaxWalkSpeed + getConfig().getDouble("threshold")) {
            failed(p);
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }
    }

}
