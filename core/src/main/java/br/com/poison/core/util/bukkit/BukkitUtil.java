package br.com.poison.core.util.bukkit;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitUtil {

    public static void removeCommand(JavaPlugin plugin, String... commands) {
        try {
            CommandMap commandMap = plugin.getServer().getCommandMap();

            Field field = commandMap.getClass().getDeclaredField("knownCommands");

            field.setAccessible(true);

            Map<String, Command> knownCommands = (HashMap<String, Command>) field.get(commandMap);

            for (String command : commands) {

                if (knownCommands.containsKey(command)) {

                    knownCommands.remove(command);

                    List<String> aliases = new ArrayList<>();

                    for (String key : knownCommands.keySet()) {
                        if (!key.contains(":"))
                            continue;

                        String substr = key.substring(key.indexOf(":") + 1);

                        if (substr.equalsIgnoreCase(command)) {
                            aliases.add(key);
                        }
                    }

                    for (String alias : aliases) {
                        knownCommands.remove(alias);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void makeRecraftInventory(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < 36; i++)
            inv.setItem(i, new ItemStack(Material.MUSHROOM_SOUP));

        inv.setItem(13, new ItemStack(Material.BOWL, 64));
        inv.setItem(14, new ItemStack(Material.RED_MUSHROOM, 64));
        inv.setItem(15, new ItemStack(Material.BROWN_MUSHROOM, 64));
    }

    public static void makeLeatherArmor(Player player, Color color) {
        PlayerInventory inv = player.getInventory();

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();

        helmetMeta.setColor(color);
        helmet.setItemMeta(helmetMeta);

        LeatherArmorMeta chestPlateMeta = (LeatherArmorMeta) chestplate.getItemMeta();

        chestPlateMeta.setColor(color);
        chestplate.setItemMeta(chestPlateMeta);

        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();

        leggingsMeta.setColor(color);
        leggings.setItemMeta(leggingsMeta);

        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();

        bootsMeta.setColor(color);
        boots.setItemMeta(bootsMeta);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    public static void makeArmorInventory(Player player, ArmorType type) {
        PlayerInventory inv = player.getInventory();

        inv.setHelmet(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_HELMET")));
        inv.setChestplate(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_CHESTPLATE")));
        inv.setLeggings(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_LEGGINGS")));
        inv.setBoots(new ItemStack(Material.getMaterial(type.name().toUpperCase() + "_BOOTS")));
    }

    public static void makeArmorInventory(Player player, ArmorType type, Enchantment enchantment) {
        PlayerInventory inv = player.getInventory();

        String name = type.name().toUpperCase();

        ItemStack helmet = new ItemStack(Material.getMaterial(name + "_HELMET"));
        helmet.addEnchantment(enchantment, 1);

        ItemStack chestplate = new ItemStack(Material.getMaterial(name + "_CHESTPLATE"));
        chestplate.addEnchantment(enchantment, 1);

        ItemStack leggings = new ItemStack(Material.getMaterial(name + "_LEGGINGS"));
        leggings.addEnchantment(enchantment, 1);

        ItemStack boots = new ItemStack(Material.getMaterial(name + "_BOOTS"));
        boots.addEnchantment(enchantment, 1);

        inv.setHelmet(helmet);
        inv.setChestplate(chestplate);
        inv.setLeggings(leggings);
        inv.setBoots(boots);
    }

    public enum ArmorType {
        LEATHER, IRON, GOLD, DIAMOND
    }
}
