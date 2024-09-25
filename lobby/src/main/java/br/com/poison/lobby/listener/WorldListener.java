package br.com.poison.lobby.listener;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.user.User;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.lobby.inventory.tracker.mode.ModeTrackerInventory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldListener implements Listener {

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Material down = player.getLocation().subtract(0.0, 1.0, 0.0).getBlock().getType();

        if (down == Material.EMERALD_BLOCK || down == Material.SLIME_BLOCK) {
            player.setVelocity(player.getLocation().getDirection().multiply(4).setY(0.6));

            player.playSound(Sound.LEVEL_UP);
        }
    }

    @EventHandler
    public void interactEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            User user = Lobby.getUserManager().read(player.getUniqueId()),
                    target = Lobby.getUserManager().read(clicked.getUniqueId());

            if (user == null || target == null) return;

            if (user.inArcade(ArcadeCategory.DUELS) && target.getArena().equals(user.getArena())
                    && player.getItemInHand().getType().equals(Material.BLAZE_ROD))
                new ModeTrackerInventory(player, clicked).init();
        }
    }

    @EventHandler
    public void onVoid(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            Player player = (Player) event.getEntity();

            User user = Lobby.getUserManager().read(player.getUniqueId());

            if (user == null) return;

            player.teleport(user.getArena().getLocation("spawn"));
        }
    }
}