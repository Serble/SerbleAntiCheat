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
import java.util.UUID;

public class BasicSpeed extends Check {
//    private final HashMap<UUID, Long> lastCheck = new HashMap<>();
//    private final HashMap<UUID, Location> lastLocation = new HashMap<>();
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
    public void onTeleport(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if (isBypassSpeed(p)) {
            return;
        }

//        lastLocation.remove(p.getUniqueId());
//        lastCheck.remove(p.getUniqueId());
        playerDetails.remove(p.getUniqueId());
    }

    @EventHandler
    public void onMove2(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (isBypassSpeed(p)) {
            return;
        }

        List<Tuple<Long, Location>> details = playerDetails.get(p.getUniqueId());
        if (details == null) {
            details = new java.util.ArrayList<>();
            playerDetails.put(p.getUniqueId(), details);
            return;
        }

        details.removeIf(tuple -> tuple.getFirst() < System.currentTimeMillis() - 1000);
        details.add(new Tuple<>(System.currentTimeMillis(), e.getTo()));
        playerDetails.put(p.getUniqueId(), details);

        Location from = details.get(0).getSecond();
        long timeSinceFrom = System.currentTimeMillis() - details.get(0).getFirst();
        Location to = e.getTo();

        int averagingPeriod = getConfig().getInt("speed-average-time-period-ms");
        if (timeSinceFrom < averagingPeriod) {
            return;  // Average must be over 1 second
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
                p.teleport(from);
            }
        }
    }

//    @EventHandler
//    public void onMove(PlayerMoveEvent e) {
//        Player p = e.getPlayer();
//        if (!canCheck(p)) return;
//
//        if (isBypassSpeed(p)) {
//            return;
//        }
//
//        Long lastCheck = this.lastCheck.get(p.getUniqueId());
//        if (lastCheck == null) {
//            lastCheck = System.currentTimeMillis();
//            this.lastCheck.put(p.getUniqueId(), System.currentTimeMillis());
//            this.lastLocation.put(p.getUniqueId(), e.getFrom());
//        }
//        long timeSinceLastCheck = System.currentTimeMillis() - lastCheck;
//
//        if (timeSinceLastCheck < 1000) {
//            return;
//        }
//
//        Location from = this.lastLocation.get(p.getUniqueId());
//        assert from != null;
//
//        this.lastCheck.put(p.getUniqueId(), System.currentTimeMillis());
//        this.lastLocation.put(p.getUniqueId(), e.getTo());
//
//        // Check if player is faster than their walk speed
//        assert e.getTo() != null;
//        Location to = e.getTo();
//
//        if (Objects.requireNonNull(from.getWorld()).getUID() != Objects.requireNonNull(to.getWorld()).getUID()) {
//            return;
//        }
//
//        double horizontalDistance = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
//        double speed = horizontalDistance / (timeSinceLastCheck / 1000.0);
//        double expectedMaxWalkSpeed = 4.317 * (p.getWalkSpeed() / 0.2);
//        expectedMaxWalkSpeed = expectedMaxWalkSpeed * 1.3 * 1.5;
//
//        if (speed > expectedMaxWalkSpeed + getConfig().getDouble("threshold")) {
//            failed(p);
//            if (Config.shouldLagBack()) {
//                e.setCancelled(true);
//                p.teleport(from);
//            }
//        }
//    }
}
