package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class QuickDropper extends Kit implements Listener {

    public QuickDropper() {
        super("QuickDropper", Material.BOWL, KitType.PRIMARY, RankCategory.VENOM,
                Collections.singletonList("§7Drope as suas sopas."),
                new ArrayList<>(),
                new ArrayList<>(),
                3200, 0L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void drop(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId())) {
            ItemStack item = player.getItemInHand();

            if (item == null || item.getType().equals(Material.AIR)) return;

            if (item.getType().equals(Material.BOWL)) {
                player.getInventory().remove(item);
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }
    }
}
