package net.serble.anticheat;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class Utils {

    public static String t(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static boolean isUsingSoulSpeed(Player p) {
        if (p.getEquipment() == null) return false;
        return p.getEquipment().getBoots() != null && p.getEquipment().getBoots().getEnchantments().containsKey(Enchantment.SOUL_SPEED);
    }

}
