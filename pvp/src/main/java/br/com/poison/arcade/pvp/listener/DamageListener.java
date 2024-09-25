package br.com.poison.arcade.pvp.listener;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.game.Game;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer(), damager = event.getDamager();

        User user = PvP.getUserManager().read(player.getUniqueId()),
                killer = PvP.getUserManager().read(damager.getUniqueId());

        if (user == null || killer == null) return;

        if (event.getFinalDamage() >= player.getHealth() && user.getGame().getCategory().equals(killer.getGame().getCategory())) {
            user.getGame().onDeath(user, killer);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity(),
                    killer = player.getKiller();

            User user = PvP.getUserManager().read(player.getUniqueId());
            if (user == null) return;

            Game game = user.getGame();
            if (game == null) return;

            if (event.getFinalDamage() >= player.getHealth()) {
                event.setCancelled(true);

                if (killer != null) {
                    User killerUser = PvP.getUserManager().read(killer.getUniqueId());

                    if (killerUser != null && killerUser.hasCombat())
                        game.onDeath(user, killerUser);
                    else
                        game.onDeath(user, null);

                } else {
                    game.onDeath(user, null);
                }
            }
        }
    }

    @EventHandler
    public void onCombat(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer(), damager = event.getDamager();

        User user = PvP.getUserManager().read(player.getUniqueId()),
                killer = PvP.getUserManager().read(damager.getUniqueId());

        if (user == null || killer == null || user.equals(killer)) return;

        if (!event.isCancelled()) {
            if (!user.isProtected() && !killer.isProtected() && player.inGameMode(GameMode.SURVIVAL) && damager.inGameMode(GameMode.SURVIVAL)) {
                user.applyCombat();
                killer.applyCombat();
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            User user = PvP.getUserManager().read(player.getUniqueId());

            if (user != null) {
                if (user.getGame().getCategory().equals(ArcadeCategory.LAVA)
                        && player.getLocation().getBlock().getRelative(BlockFace.SELF).getType().name().contains("LAVA")) {
                    event.setCancelled(false);
                } else {
                    event.setCancelled(user.isProtected());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            User user = PvP.getUserManager().read(event.getEntity().getUniqueId()),
                    damagerUser = PvP.getUserManager().read(event.getDamager().getUniqueId());

            if (user == null || damagerUser == null || user.equals(damagerUser)) return;

            if (user.isProtected() || damagerUser.isProtected())
                event.setCancelled(true);
        }
    }
}