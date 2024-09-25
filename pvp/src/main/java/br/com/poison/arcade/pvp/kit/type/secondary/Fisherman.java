package br.com.poison.arcade.pvp.kit.type.secondary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.ArrayList;
import java.util.Collections;

public class Fisherman extends Kit implements Listener {

    public Fisherman() {
        super("Fisherman", Material.FISHING_ROD, KitType.SECONDARY, RankCategory.VENOM,
                Collections.singletonList("§7Fisgue seus inimigos."),
                Collections.singletonList(new Item(Material.FISHING_ROD).name("§aFisgue!")),
                new ArrayList<>(),
                4500, 0L);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof LivingEntity))
            return;

        Player player = event.getPlayer();

        if (isUsingKit(player.getUniqueId())) {
            if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY)
                event.getCaught().teleport(player.getLocation());

            player.getItemInHand().setDurability((short) 0);
        }
    }
}
