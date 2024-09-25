package br.com.poison.arcade.pvp.kit.type.secondary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Collections;

public class Fireman extends Kit implements Listener {

    public Fireman() {
        super("Fireman", Material.LAVA_BUCKET, KitType.SECONDARY, RankCategory.PLAYER,
                Collections.singletonList("§7Não receba dano ao fogo."),
                new ArrayList<>(),
                new ArrayList<>(),
                0, 0L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (isUsingKit(player.getUniqueId()) && (event.getCause().equals(EntityDamageEvent.DamageCause.LAVA) || event.getCause().name().startsWith("FIRE")))
                event.setCancelled(true);
        }
    }
}
