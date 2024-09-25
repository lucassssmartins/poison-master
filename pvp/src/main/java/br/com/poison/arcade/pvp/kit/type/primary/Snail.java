package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;

public class Snail extends Kit implements Listener {

    public Snail() {
        super("Snail", Material.SLIME_BLOCK, KitType.PRIMARY, RankCategory.PLAYER,
                Collections.singletonList("§7Tenha chance de causar lentidão."),
                new ArrayList<>(),
                new ArrayList<>(),
                0, 0L);
    }

    @EventHandler
    public void onSnail(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer(), damager = event.getDamager();

        if (isUsingKit(damager.getUniqueId()) && isAllowReceive(player.getUniqueId()) && Core.RANDOM.nextInt(4) == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 3 * 20, 0));
        }
    }
}
