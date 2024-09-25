package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.api.mechanics.item.Item;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class Monk extends Kit implements Listener {

    public Monk() {
        super("Monk", Material.BLAZE_ROD, KitType.PRIMARY, RankCategory.VENOM,
                Collections.singletonList("§7Bagunce o inventário dos seus inimigos."),
                Collections.singletonList(new Item(Material.BLAZE_ROD).name("§aMonk")),
                new ArrayList<>(),
                6000, 13L);
    }

    @EventHandler
    public void onMonk(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof Player) {
            Player player = event.getPlayer(), clicked = (Player) event.getRightClicked();

            if (hasCooldown(player)) return;

            if (isUsingKit(player.getUniqueId()) && isKitItem(player.getItemInHand())) {
                int randomSlot = Core.RANDOM.nextInt(36);

                ItemStack current = (clicked.getItemInHand() != null ? clicked.getItemInHand().clone() : null),
                        random = (clicked.getInventory().getItem(randomSlot) != null ? clicked.getInventory().getItem(randomSlot).clone() : null);

                if (random == null) {
                    clicked.getInventory().setItem(randomSlot, current);
                    clicked.setItemInHand(null);
                } else {
                    clicked.getInventory().setItem(randomSlot, current);
                    clicked.getInventory().setItemInHand(random);
                }

                addCooldown(player, getCooldown());

                player.sendMessage("§6§lMONK §eVocê bagunçou o inventário de §b" + clicked.getName() + "§e!");

                clicked.sendMessage("§6§lMONK §eO seu inventário foi bagunçado por §b" + player.getName() + "§e!");
                clicked.playSound(clicked.getLocation(), Sound.PISTON_RETRACT, 1.5f, 2.0f);
            }
        }
    }
}
