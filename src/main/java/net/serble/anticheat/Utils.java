package net.serble.anticheat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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

    public static void sendActionBarMessage(Player p, String msg) {
        BaseComponent[] components = {
                new TextComponent(t(msg)),
        };
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }

}
