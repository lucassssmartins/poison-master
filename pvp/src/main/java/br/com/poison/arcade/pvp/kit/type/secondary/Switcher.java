package br.com.poison.arcade.pvp.kit.type.secondary;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.Collections;

public class Switcher extends Kit implements Listener {

    public Switcher() {
        super("Switcher", Material.SNOW_BALL, KitType.SECONDARY, RankCategory.PLAYER,
                Collections.singletonList("§7Troque de lugar com seu oponente."),
                Collections.singletonList(new Item(Material.SNOW_BALL).name("§aSwitcher")),
                new ArrayList<>(),
                0, 8L);
    }

    @EventHandler
    public void use(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack item = player.getItemInHand();

        if (item == null || item.getType().equals(Material.AIR)) return;

        if (event.getAction().name().contains("RIGHT")) {

            if (isUsingKit(player.getUniqueId()) && isKitItem(item)) {
                event.setCancelled(true);

                player.updateInventory();

                if (hasCooldown(player)) return;
                else addCooldown(player, getCooldown());

                Snowball snowball = player.launchProjectile(Snowball.class);

                snowball.setMetadata("switch", new FixedMetadataValue(PvP.getPlugin(PvP.class), player));
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager().hasMetadata("switch")) {
            Player player = (Player) event.getDamager().getMetadata("switch").get(0).value();
            if (player == null) return;

            if (isUsingKit(player.getUniqueId()) && isAllowReceive(event.getEntity().getUniqueId())) {
                Location loc = event.getEntity().getLocation().clone();

                event.getEntity().teleport(player.getLocation().clone());

                player.teleport(loc);
            }
        }
    }
}
