package br.com.poison.core.bukkit.listener;

import br.com.poison.core.bukkit.event.list.cooldown.CooldownEndEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.AsyncUpdateEvent;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.user.cooldown.CooldownManager;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import br.com.poison.core.bukkit.api.user.cooldown.entities.ItemCooldown;
import br.com.poison.core.bukkit.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class CooldownListener implements Listener {

    private final CooldownManager manager = BukkitCore.getCooldownManager();

    @EventHandler
    public void onCooldown(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.TICK)) {
            if (event.getTicks() % 5 == 0) return;

            for (UUID uuid : manager.getCooldownMap().keySet()) {
                Player player = Bukkit.getPlayer(uuid);

                if (player != null) {
                    List<Cooldown> list = manager.getCooldownMap().get(uuid);
                    Iterator<Cooldown> it = list.iterator();

                    /* Get Cooldown */
                    Cooldown search = null;

                    while (it.hasNext()) {
                        Cooldown cooldown = it.next();

                        if (!cooldown.expired()) {
                            if (cooldown instanceof ItemCooldown) {
                                ItemStack item = player.getItemInHand();

                                if (item != null && !item.getType().equals(Material.AIR)) {
                                    ItemCooldown itemCooldown = (ItemCooldown) cooldown;

                                    if (item.isSimilar(itemCooldown.getItem())) {
                                        itemCooldown.setSelected(true);

                                        search = itemCooldown;
                                        break;
                                    }
                                }

                                continue;
                            }

                            search = cooldown;
                            continue;
                        }

                        it.remove();

                        new CooldownEndEvent(player, cooldown).call();
                    }

                    /* Display Cooldown */
                    if (search != null) {
                        manager.display(player, search);
                    } else if (list.isEmpty()) {
                        PlayerManager.sendBar(player, " ");
                        manager.getCooldownMap().remove(player.getUniqueId());
                    } else {
                        Cooldown cooldown = list.get(0);

                        if (cooldown instanceof ItemCooldown) {
                            ItemCooldown item = (ItemCooldown) cooldown;

                            if (item.isSelected()) {
                                PlayerManager.sendBar(player, " ");

                                item.setSelected(false);
                            }
                        }
                    }
                }
            }
        }
    }
}