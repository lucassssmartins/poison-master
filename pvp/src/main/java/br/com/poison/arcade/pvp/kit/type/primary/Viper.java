package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Viper extends Kit implements Listener {

    public Viper() {
        super("Viper", Material.SPIDER_EYE, KitType.PRIMARY, RankCategory.PLAYER,
                Arrays.asList("§7Tenha chance de envenenar", "§7seus inimigos."),
                new ArrayList<>(),
                Collections.singletonList("boxer"),
                0, 0);
    }

    @EventHandler
    public void onViper(PlayerDamagePlayerEvent event) {
        Player player = event.getPlayer(), damager = event.getDamager();

        if (isUsingKit(damager.getUniqueId()) && isAllowReceive(player.getUniqueId())) {
            if (Core.RANDOM.nextInt(5) == 2) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 80, 0));

                player.getLocation().getWorld().playEffect(player.getLocation().clone().add(0.0, 0.4, 0.0), Effect.STEP_SOUND, 159, 13);
            }
        }
    }

}
