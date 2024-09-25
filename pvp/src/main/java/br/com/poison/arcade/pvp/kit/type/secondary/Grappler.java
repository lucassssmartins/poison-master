package br.com.poison.arcade.pvp.kit.type.secondary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.kit.type.secondary.grappler.GrapplingHook;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class Grappler extends Kit implements Listener {

    private final Map<UUID, GrapplingHook> grapplerHooks;

    public Grappler() {
        super("Grappler", Material.LEASH, KitType.SECONDARY, RankCategory.VENOM,
                Collections.singletonList("§7Locomova-se rapidamente com sua corda."),
                Collections.singletonList(new Item(Material.LEASH).name("§aGrappler")),
                Arrays.asList("stomper", "kangaroo"),
                5200, 8L);

        grapplerHooks = new HashMap<>();
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (isUsingKit(event.getPlayer().getUniqueId()) && event.getItem() != null) {

            Action action = event.getAction();
            Player player = event.getPlayer();
            ItemStack item = player.getItemInHand();

            if (!isKitItem(item))
                return;

            if (action.name().contains("RIGHT")) {
                event.setCancelled(true);
            }

            item.setDurability((short) 0);
            player.updateInventory();

            if (hasCooldown(player))
                return;

            if (event.getAction().name().contains("LEFT")) {
                if (grapplerHooks.containsKey(player.getUniqueId())) {
                    grapplerHooks.get(player.getUniqueId()).remove();
                    grapplerHooks.remove(player.getUniqueId());
                }
                GrapplingHook hook = new GrapplingHook(player.getWorld(), ((CraftPlayer) player).getHandle());
                Vector direction = player.getLocation().getDirection();
                hook.spawn(player.getEyeLocation().add(direction.getX(), direction.getY(), direction.getZ()));
                hook.move(direction.getX() * 7.0D, direction.getY() * 5.0D, direction.getZ() * 7.0D);
                grapplerHooks.put(player.getUniqueId(), hook);
            } else if (event.getAction().name().contains("RIGHT")) {
                if (grapplerHooks.containsKey(player.getUniqueId())) {
                    if (!grapplerHooks.get(player.getUniqueId()).isHooked())
                        return;

                    GrapplingHook hook = grapplerHooks.get(player.getUniqueId());
                    Location loc = hook.getBukkitEntity().getLocation();
                    Location pLoc = player.getLocation();
                    double t = loc.distance(player.getLocation());
                    double v_x = (1.0D + 0.06D * t) * ((isNear(loc, pLoc) ? 0 : loc.getX() - pLoc.getX()) / t);
                    double v_y = (0.9D + 0.03D * t) * ((isNear(loc, pLoc) ? 0.1 : loc.getY() - pLoc.getY()) / t);
                    double v_z = (1.0D + 0.06D * t) * ((isNear(loc, pLoc) ? 0 : loc.getZ() - pLoc.getZ()) / t);
                    Vector v = player.getVelocity();
                    v.setX(v_x);
                    v.setY(v_y);
                    v.setZ(v_z);

                    player.setVelocity(v.multiply(1));

                    player.getWorld().playSound(player.getLocation(), Sound.STEP_GRAVEL, 1.0F, 1.0F);
                    player.setFallDistance(0f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        if (!isUsingKit(event.getPlayer().getUniqueId()))
            return;

        Player p = event.getPlayer();

        if (p.getItemInHand() == null)
            return;

        ItemStack item = p.getItemInHand();

        if (!isKitItem(item))
            return;

        item.setDurability((short) 0);
        event.setCancelled(true);
        if (grapplerHooks.containsKey(p.getUniqueId())) {
            if (grapplerHooks.get(p.getUniqueId()).isHooked()) {
                GrapplingHook hook = grapplerHooks.get(p.getUniqueId());
                Location loc = hook.getBukkitEntity().getLocation();
                Location playerLoc = p.getLocation();
                double t = loc.distance(playerLoc);
                double v_x = (1.0D + 0.04000000000000001D * t)
                        * ((isNear(loc, playerLoc) ? 0 : loc.getX() - playerLoc.getX()) / t);
                double v_y = (0.9D + 0.03D * t) * ((isNear(loc, playerLoc) ? 0.1 : loc.getY() - playerLoc.getY()) / t);
                double v_z = (1.0D + 0.04000000000000001D * t)
                        * ((isNear(loc, playerLoc) ? 0 : loc.getZ() - playerLoc.getZ()) / t);
                Vector v = p.getVelocity();
                v.setX(v_x);
                v.setY(v_y);
                v.setZ(v_z);
                p.setVelocity(v.multiply(1));

                if (playerLoc.getY() < hook.getBukkitEntity().getLocation().getY()) {
                    p.setFallDistance(0);
                }

                p.getWorld().playSound(playerLoc, Sound.STEP_GRAVEL, 1.0F, 1.0F);
            }
        }
    }

    private boolean isNear(Location loc, Location playerLoc) {
        return loc.distance(playerLoc) < 1.5;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        if (grapplerHooks.containsKey(e.getPlayer().getUniqueId())) {
            grapplerHooks.get(e.getPlayer().getUniqueId()).remove();
            grapplerHooks.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (grapplerHooks.containsKey(e.getPlayer().getUniqueId())) {
            grapplerHooks.get(e.getPlayer().getUniqueId()).remove();
            grapplerHooks.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId()))
            addCooldown(player, getCooldown());
    }
}
