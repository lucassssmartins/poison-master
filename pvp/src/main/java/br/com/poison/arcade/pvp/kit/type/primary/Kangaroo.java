package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class Kangaroo extends Kit implements Listener {

    private final List<UUID> jumpList;

    public Kangaroo() {
        super("Kangaroo", Material.FIREWORK, KitType.PRIMARY, RankCategory.PLAYER,
                Collections.singletonList("§7Pule igual um Canguru."),
                Collections.singletonList(new Item(Material.FIREWORK).name("§aCanguru")),
                Arrays.asList("stomper", "grappler"),
                0, 8L);

        jumpList = new ArrayList<>();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId()) && event.getAction() != Action.PHYSICAL && isKitItem(event.getItem())) {
            event.setCancelled(true);

            if (jumpList.contains(player.getUniqueId()))
                return;

            if (hasCooldown(player))
                return;

            Vector vector = player.getEyeLocation().getDirection().multiply(player.isSneaking() ? 2.3F : 0.7f).setY(player.isSneaking() ? 0.5 : 1F);

            player.setFallDistance(-1.0F);
            player.setVelocity(vector);

            jumpList.add(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJumpRemove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId()) && jumpList.contains(player.getUniqueId()) && player.isOnGround())
            jumpList.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(PlayerDamagePlayerEvent event) {
        if (isUsingKit(event.getPlayer().getUniqueId()))
            addCooldown(event.getPlayer(), getCooldown());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            Player player = (Player) event.getEntity();

            if (isUsingKit(player.getUniqueId())) {
                if (jumpList.contains(player.getUniqueId())) {
                    jumpList.remove(player.getUniqueId());

                    event.setCancelled(true);
                } else
                    event.setDamage(1.0D);
            }
        }
    }
}
