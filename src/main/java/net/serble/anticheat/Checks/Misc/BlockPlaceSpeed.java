package net.serble.anticheat.Checks.Misc;

import net.serble.anticheat.Check;
import net.serble.anticheat.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BlockPlaceSpeed extends Check {

    private final HashMap<UUID, List<Long>> blocksPlaced = new HashMap<>();

    @Override
    public String getName() {
        return "BlockPlaceSpeed";
    }

    @Override
    public String getConfigName() {
        return "block-place-speed";
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // Put a timestamp (the long value) in the hashmap for the block break and check if the player has broken 3 blocks in the last 1 second
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (!canCheck(p)) return;

        Long timestamp = System.currentTimeMillis();
        if (!blocksPlaced.containsKey(playerId)) {
            blocksPlaced.put(playerId, new ArrayList<>(List.of(timestamp)));
            return;
        }

        blocksPlaced.get(playerId).add(timestamp);

        int sampleTime = getConfig().getInt("sample-time");

        // Remove timestamps older than 1 second
        blocksPlaced.get(playerId).removeIf(t -> t < System.currentTimeMillis() - sampleTime);

        // Check if the player has broken more than 3 blocks in the last second
        double maxBlocksInSampleTime = sampleTime * ((double) getConfig().getInt("max-blocks-per-second") / 1000.0D);
        if (blocksPlaced.get(playerId).size() > maxBlocksInSampleTime) {
            failed(p);
            if (Config.shouldLagBack()) {
                e.setCancelled(true);
            }
        }
    }

}
