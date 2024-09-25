package br.com.poison.arcade.duels.game.list.standard.simulator.kit.list;

import br.com.poison.arcade.duels.Duels;
import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Anchor extends Kit implements Listener {

    public Anchor() {
        super("Anchor", "Não dê e nem receba knockback.", Material.ANVIL, 0);
    }

    @EventHandler
    public void damage(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer(), damager = event.getDamager();

        if (isUsingKit(player.getUniqueId()) || isUsingKit(damager.getUniqueId())) {
            applyVelocity(player);
        }
    }

    protected void applyVelocity(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setVelocity(new Vector(0.0, -1, 0.0));
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 0.5f, 1.5f);

                cancel();
            }
        }.runTaskLater(Duels.getPlugin(Duels.class), 1L);
    }
}
