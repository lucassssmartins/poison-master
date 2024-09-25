package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class Flash extends Kit implements Listener {

    public Flash() {
        super("Flash", Material.REDSTONE_TORCH_ON, KitType.PRIMARY, RankCategory.VENOM,
                Collections.singletonList("§7Teleporte-se como o Flash."),
                Collections.singletonList(new Item(Material.REDSTONE_TORCH_ON).name("§aFlash!")),
                new ArrayList<>(),
                6200,
                25L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId()) && isKitItem(player.getItemInHand())) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                event.getPlayer().updateInventory();
                return;
            }

            if (event.getAction() != Action.RIGHT_CLICK_AIR)
                return;

            if (hasCooldown(player))
                return;

            event.setCancelled(true);

            Block block = player.getTargetBlock((Set<Material>) null, 100);
            Location location = block.getWorld().getHighestBlockAt(block.getLocation()).getLocation();

            BlockIterator list = new BlockIterator(player.getEyeLocation(), 0, 100);

            while (list.hasNext()) {
                player.getWorld().playEffect(list.next().getLocation(), Effect.ENDER_SIGNAL, 100);
            }

            player.teleport(location.clone().add(0, 1.5, 0));
            player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);

            addCooldown(player, getCooldown());
        }
    }
}
