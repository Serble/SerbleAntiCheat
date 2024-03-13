package net.serble.anticheat;

import net.serble.anticheat.Schemas.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ViolationsManager {
    private final HashMap<UUID, List<Tuple<Long, Integer>>> violations = new HashMap<>();

    public void addViolation(String check, Player p, int weight) {
        UUID playerId = p.getUniqueId();

        // Add the new violation
        if (violations.containsKey(playerId)) {
            violations.get(playerId).add(new Tuple<>(System.currentTimeMillis(), weight));
        } else {
            List<Tuple<Long, Integer>> list = new java.util.ArrayList<>();
            list.add(new Tuple<>(System.currentTimeMillis(), weight));
            violations.put(playerId, list);
        }

        p.sendMessage(Utils.t("&cYou failed &e" + check + "&c! &7Violation score: &e" + getViolationScore(playerId) + "&7/" + Config.getConfiguration().getInt("violation-score-threshold-pm")));

        if (getViolationScore(playerId) > Config.getConfiguration().getInt("violation-score-threshold-pm")) {
            //p.kickPlayer("You have been kicked for cheating.");
            Bukkit.broadcastMessage(Utils.t("&c" + p.getName() + " &7has been kicked for cheating."));
        }
    }

    private int getViolationScore(UUID playerId) {
        if (violations.containsKey(playerId)) {
            violations.get(playerId).removeIf(tuple -> tuple.getFirst() < System.currentTimeMillis() - 60000);
            return violations.get(playerId).stream().mapToInt(Tuple::getSecond).sum();
        }
        return 0;
    }

}
