package net.serble.anticheat.Checks.Movement;

import net.serble.anticheat.Check;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class NoFall extends Check {
    private final HashMap<UUID, Double> fallDistance = new HashMap<>();
    private final HashMap<UUID, Long> aboutToTakeDamage = new HashMap<>();
    private final HashMap<UUID, Long> lastFallDamage = new HashMap<>();
    private final HashMap<UUID, Double> amountOfFallDamage = new HashMap<>();

    @Override
    public String getName() {
        return "NoFall";
    }

    @Override
    public String getConfigName() {
        return "nofall";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getPlayer().isInsideVehicle()) {
            fallDistance.remove(e.getPlayer().getUniqueId());
            aboutToTakeDamage.remove(e.getPlayer().getUniqueId());
            return;
        }
        Player p = e.getPlayer();
        if (!canCheck(p)) return;

        if (aboutToTakeDamage.containsKey(p.getUniqueId())) {
            // Check time since last fall
            if (System.currentTimeMillis() - aboutToTakeDamage.get(p.getUniqueId()) > getConfig().getInt("time-until-fail")) {
                // They should take damage

                // If they took damage in the last 1 second, return
                if (lastFallDamage.containsKey(p.getUniqueId()) && System.currentTimeMillis() - lastFallDamage.get(p.getUniqueId()) < 1000) {
                    aboutToTakeDamage.remove(p.getUniqueId());
                    return;
                }

                failed(p);
                if (getConfig().getBoolean("enforce-fall-damage")) p.damage(amountOfFallDamage.get(p.getUniqueId()));
                aboutToTakeDamage.remove(p.getUniqueId());
            }
        }

        boolean isOnGround = isOnGround(p);

        // Add any fall distance
        if (e.getFrom().getY() > Objects.requireNonNull(e.getTo()).getY() && !isOnGround && !p.getAllowFlight()) {
            Double lastFallDistance = fallDistance.get(p.getUniqueId());
            if (lastFallDistance == null) {
                lastFallDistance = 0.0;
            }
            fallDistance.put(p.getUniqueId(), lastFallDistance + (e.getFrom().getY() - e.getTo().getY()));
        }

        if (isNegateFallDamage(p)) {
            fallDistance.remove(p.getUniqueId());
            return;
        }

        if (isOnGround && !p.getAllowFlight() && !p.isInsideVehicle()) {
            Double lastFallDistance = fallDistance.get(p.getUniqueId());
            if (lastFallDistance == null) {
                lastFallDistance = 0.0;
            }
            if (lastFallDistance < 4.0) {
                fallDistance.remove(p.getUniqueId());
                return;
            }

            // They should take damage
            aboutToTakeDamage.put(p.getUniqueId(), System.currentTimeMillis());
            amountOfFallDamage.put(p.getUniqueId(), lastFallDistance-3);
            fallDistance.remove(p.getUniqueId());
        }


    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player p = (Player) e.getEntity();
        aboutToTakeDamage.remove(p.getUniqueId());
        lastFallDamage.put(p.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        cancelFallDistance(e.getPlayer());
    }

    @EventHandler
    public void onDie(PlayerDeathEvent e) {
        cancelFallDistance(e.getEntity());
    }

    private void cancelFallDistance(Player p) {
        fallDistance.remove(p.getUniqueId());
        aboutToTakeDamage.remove(p.getUniqueId());
    }
}
