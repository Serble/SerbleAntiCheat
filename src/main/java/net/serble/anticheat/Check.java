package net.serble.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public abstract class Check implements Listener {

    public void register() {
        Main.getInstance().getChecksManager().register(this);
        Bukkit.getServer().getPluginManager().registerEvents(this, Main.getInstance());
    }

    public void failed(Player p) {
        Main.getInstance().getViolationsManager().addViolation(getName(), p, getConfig().getInt("violation-strength"));
    }

    public abstract String getName();

    public abstract String getConfigName();

    public void debug(Player p, String message) {
        if (debugging()) {
            p.sendMessage(Utils.t("&c[AC " + getName() + "] &e" + message));
        }
    }

    public boolean debugging() {
        return Config.amIDebugging(this);
    }

    public ConfigurationSection getConfig() {
        return Config.getConfiguration().getConfigurationSection("checks." + getConfigName());
    }

    public boolean isClimbing(Player p) {
        return p.getLocation().getBlock().getType() == Material.LADDER ||
                p.getLocation().getBlock().getType() == Material.VINE ||
                p.getLocation().getBlock().getType() == Material.WATER ||
                p.getLocation().getBlock().getType() == Material.LAVA ||
                p.getLocation().getBlock().getType().toString().contains("VINES");
    }

    public boolean canCheck(Player p) {
        List<String> worlds = Config.getConfiguration().getStringList("disabled-worlds.worlds");
        boolean isWhitelist = Config.getConfiguration().getBoolean("disabled-worlds.is-whitelist");
        boolean playerInWorlds = worlds.contains(p.getWorld().getName());
        if (playerInWorlds && !isWhitelist) {
            return false;
        }
        if (!playerInWorlds && isWhitelist) {
            return false;
        }
        double tps = TpsTracker.getTPS();
        if (tps < Config.getConfiguration().getDouble("tps-threshold")) {
            if (Config.debug()) {
                Utils.sendActionBarMessage(p, "&cChecks are disabled due to low TPS! &7(" + tps + " TPS)");
            }
            return false;
        } else {
            if (Config.debug()) {
                Utils.sendActionBarMessage(p, "&aChecks are enabled! &7(" + tps + " TPS)");
            }
        }
        if (p.getPing() > Config.getConfiguration().getInt("ping-threshold")) {
            return false;
        }
        return true;
    }

    public List<Material> getBlocksPlayerIsStandingOn(Player p, int yOffset) {
        List<Material> materialList = new ArrayList<>();
        Location playerLocation = p.getLocation();
        double x = playerLocation.getX();
        double y = playerLocation.getY() + yOffset;
        double z = playerLocation.getZ();

        // get 4 corners of the player hitbox
        Location corner1 = new Location(playerLocation.getWorld(), x + 0.3, y-1, z + 0.3);
        Location corner2 = new Location(playerLocation.getWorld(), x - 0.3, y-1, z + 0.3);
        Location corner3 = new Location(playerLocation.getWorld(), x + 0.3, y-1, z - 0.3);
        Location corner4 = new Location(playerLocation.getWorld(), x - 0.3, y-1, z - 0.3);

        // add the blocks to the list
        materialList.add(corner1.getBlock().getType());
        materialList.add(corner2.getBlock().getType());
        materialList.add(corner3.getBlock().getType());
        materialList.add(corner4.getBlock().getType());
        return materialList;
    }

    public List<Material> getBlocksPlayerIsStandingOn(Player p) {
        return getBlocksPlayerIsStandingOn(p, 0);
    }

    public List<Material> getBlocksPlayerIsStandingOnAndAbove(Player p, int upAmount, boolean ignoreFeet) {
        List<Material> materialList = new ArrayList<>();
        for (int i = ignoreFeet ? 1 : 0; i < upAmount; i++) {
            materialList.addAll(getBlocksPlayerIsStandingOn(p, i));
        }
        return materialList;
    }

    public boolean isOnGround(Player p) {
        return getBlocksPlayerIsStandingOn(p).stream().anyMatch(b -> b != Material.AIR);
    }

    public boolean isNegateFallDamage(Player p) {
        List<Material> standingOn = getBlocksPlayerIsStandingOnAndAbove(p, 2, false);
        return isInBlock(Material.WATER, standingOn) ||
                isInBlock(Material.LAVA, standingOn) ||
                isInBlock(Material.SLIME_BLOCK, standingOn) ||
                isInBlock(Material.HONEY_BLOCK, standingOn) ||
                isInBlock(Material.COBWEB, standingOn) ||
                isInBlock(Material.VINE, standingOn) ||
                isInBlock(Material.LADDER, standingOn) ||
                isInBlock(Material.WEEPING_VINES, standingOn) ||
                isInBlock(Material.TWISTING_VINES, standingOn) ||
                isInBlock(Material.WEEPING_VINES_PLANT, standingOn) ||
                isInBlock(Material.TWISTING_VINES_PLANT, standingOn);
    }

    public boolean isInBlock(Player p, Material material) {
        List<Material> materials = getBlocksPlayerIsStandingOn(p);
        return materials.stream().anyMatch(b -> b == material);
    }

    public boolean isInBlock(Material material, List<Material> standingOn) {
        return standingOn.stream().anyMatch(b -> b == material);
    }

    public boolean isBypassSpeed(Player p) {
        if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) {
            return true;
        }
        if (p.isRiptiding()) {
            return true;
        }
        if (p.isFlying()) {
            return true;
        }
        if (p.isGliding()) {
            return true;
        }
        if (p.isInsideVehicle()) {
            return true;
        }
        if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            return true;
        }
        if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP)) {
            return true;
        }
        if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE)) {
            return true;
        }
        if (isInBubbleColumn(p)) {
            return true;
        }
        // TODO: Check for pvp
        if (isOnGround(p)) {
            List<Material> standingOn = getBlocksPlayerIsStandingOn(p);
            if ((isInBlock(Material.SOUL_SAND, standingOn) || isInBlock(Material.SOUL_SOIL, standingOn)) && Utils.isUsingSoulSpeed(p)) {
                return true;
            }
            if (isInBlock(Material.ICE, standingOn)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBypassFly(Player p) {
        if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) {
            return true;
        }
        if (p.isFlying()) {
            return true;
        }
        if (p.isGliding()) {
            return true;
        }
        if (p.isRiptiding()) {
            return true;
        }
        if (p.isInsideVehicle()) {
            return true;
        }
        if (p.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP)) {
            return true;
        }
        return false;
    }

    public boolean isInBubbleColumn(Player p) {
        List<Material> standingOn = getBlocksPlayerIsStandingOn(p);
        return isInBlock(Material.BUBBLE_COLUMN, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL_BLOCK, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL_FAN, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL_WALL_FAN, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL_FAN, standingOn) ||
                isInBlock(Material.BUBBLE_CORAL_WALL_FAN, standingOn);
    }

}
