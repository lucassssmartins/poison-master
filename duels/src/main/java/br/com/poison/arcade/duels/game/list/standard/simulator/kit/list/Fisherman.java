package br.com.poison.arcade.duels.game.list.standard.simulator.kit.list;

import br.com.poison.arcade.duels.game.list.standard.simulator.kit.Kit;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Collections;

public class Fisherman extends Kit implements Listener {

    public Fisherman() {
        super("Fisherman", "Fisgue os seus inimigos.", Material.FISHING_ROD, 0,
                Collections.singletonList(new Item(Material.FISHING_ROD).name("§aFisherman!")));
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
