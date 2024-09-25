package br.com.poison.arcade.pvp.kit.type.secondary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.Collections;

public class Boxer extends Kit implements Listener {

    public Boxer() {
        super("Boxer", Material.NETHER_STALK, KitType.SECONDARY, RankCategory.VENOM,
                Collections.singletonList("§7Dê mais dano com nos seus inimigos."),
                new ArrayList<>(),
                Collections.singletonList("viper"),
                3500, 0L);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            if (isUsingKit(event.getEntity().getUniqueId()) && event.getDamage() > 1.0D)
                event.setDamage(event.getDamage() - 0.25D);

            if (isUsingKit(player.getUniqueId()) && player.getItemInHand().getType() == Material.AIR) {
                event.setDamage(event.getDamage() + 2.0D);
                return;
            }

            if (isUsingKit(player.getUniqueId()) && player.getItemInHand().getType() != Material.AIR)
                event.setDamage(event.getDamage() + 0.25D);
        }
    }
}
