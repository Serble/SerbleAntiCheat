package net.serble.anticheat.Checks.Misc;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.*;

public class BlockBreakSpeed extends Check {

    private final HashMap<UUID, List<Long>> blocksBroken = new HashMap<>();

    @Override
    public String getName() {
        return "BlockBreakSpeed";
    }

    @Override
    public String getConfigName() {
        return "nuker";
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Put a timestamp (the long value) in the hashmap for the block break and check if the player has broken 3 blocks in the last 1 second
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (!canCheck(p)) return;

        Long timestamp = System.currentTimeMillis();
        if (!blocksBroken.containsKey(playerId)) {
            blocksBroken.put(playerId, new ArrayList<>(List.of(timestamp)));
            return;
        }

        blocksBroken.get(playerId).add(timestamp);

        int sampleTime = getConfig().getInt("sample-time");

        // Remove timestamps older than 1 second
        blocksBroken.get(playerId).removeIf(t -> t < System.currentTimeMillis() - sampleTime);

        // Check if the player has broken more than 3 blocks in the last second
        double maxBlocksInSampleTime = sampleTime * ((double) getConfig().getInt("max-blocks-per-second") / 1000.0D);
        if (blocksBroken.get(playerId).size() >= maxBlocksInSampleTime) {
            failed(p);
            p.sendMessage("You broke " + blocksBroken.get(playerId).size() + " blocks in " + sampleTime + "ms");
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }
    }

}
