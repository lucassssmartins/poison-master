package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;

public class Anchor extends Kit implements Listener {

    public Anchor() {
        super("Anchor", Material.ANVIL, KitType.PRIMARY, RankCategory.PLAYER,
                Collections.singletonList("§7Não de e nem receba knockback."),
                new ArrayList<>(),
                new ArrayList<>(),
                0, 0L);
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
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1.5f, 2f);

                cancel();
            }
        }.runTaskLater(PvP.getPlugin(PvP.class), 1L);
    }
}
