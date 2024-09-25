package br.com.poison.arcade.duels.game.list.standard.simulator.kit.list;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.Collections;
import java.util.Set;

public class Thor extends Kit implements Listener {

    public Thor() {
        super("Thor", "Dê trovão nos seus inimigos.", Material.WOOD_AXE, 12,
                Collections.singletonList(new Item(Material.WOOD_AXE).name("§aThor!")));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getItemInHand();

        if (item == null || item.getType().equals(Material.AIR)) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (isUsingKit(player.getUniqueId()) && isKitItem(item)) {

                if (hasCooldown(player)) return;
                else
                    addCooldown(player, getCooldown());

                Location location = player.getTargetBlock((Set<Material>) null, 25).getLocation();

                player.getWorld().strikeLightning(location);
                player.getWorld().setMetadata("thor", new FixedMetadataValue(Duels.getPlugin(Duels.class), System.currentTimeMillis() + 4000L));

                if (location.getBlock().getY() >= 110) {
                    Location newLocation = location.clone();

                    if (newLocation.getBlock().getType() == Material.NETHERRACK) {
                        newLocation.getWorld().createExplosion(newLocation, 2.5F);
                    }
                }

                item.setDurability((short) 0);

                player.updateInventory();

                event.setCancelled(true);
            }

        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();

            if (event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING)) {

                MetadataValue value = player.getMetadata("thor").stream().findFirst().orElse(null);

                if (value == null) {
                    event.setDamage(3.0D);
                    event.getEntity().setFireTicks(200);
                } else if (value.asLong() > System.currentTimeMillis()) {
                    event.setCancelled(true);
                    value.invalidate();
                }

            }
        }
    }
}
