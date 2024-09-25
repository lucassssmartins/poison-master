package br.com.poison.arcade.pvp.kit.type.primary;

import br.com.poison.arcade.pvp.PvP;
import br.com.poison.arcade.pvp.kit.Kit;
import br.com.poison.arcade.pvp.kit.type.KitType;
import br.com.poison.arcade.pvp.kit.type.primary.ninja.NinjaEntry;
import br.com.poison.arcade.pvp.user.User;
import br.com.poison.core.bukkit.event.list.damage.PlayerDamagePlayerEvent;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Ninja extends Kit implements Listener {

    private final Map<UUID, NinjaEntry> ninjaEntryMap = new ConcurrentHashMap<>();

    public Ninja() {
        super("Ninja", Material.EMERALD, KitType.PRIMARY, RankCategory.VENOM,
                Collections.singletonList("§7Vá atrás dos seus inimigos."),
                new ArrayList<>(),
                new ArrayList<>(),
                6530, 8L);
    }

    @EventHandler
    public void checkEntries(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Map.Entry<UUID, NinjaEntry> entry : ninjaEntryMap.entrySet()) {
                NinjaEntry ninja = entry.getValue();

                if (ninja == null) continue;

                if (ninja.hasExpired())
                    ninjaEntryMap.remove(entry.getKey());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(PlayerDamagePlayerEvent event) {
        Player damager = event.getDamager();

        User user = PvP.getUserManager().read(damager.getUniqueId());

        if (user == null) return;

        if (isUsingKit(damager.getUniqueId())) {
            if (!ninjaEntryMap.containsKey(damager.getUniqueId()))
                ninjaEntryMap.put(damager.getUniqueId(), new NinjaEntry(event.getPlayer()));
            else {
                NinjaEntry entry = ninjaEntryMap.get(damager.getUniqueId());

                if (entry != null) {

                    if (!entry.getTarget().equals(event.getPlayer()))
                        entry.setTarget(event.getPlayer());

                    entry.setExpiresAt(System.currentTimeMillis() + 10000L);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        User user = PvP.getUserManager().read(player.getUniqueId());

        if (user == null) return;

        if (isUsingKit(player.getUniqueId())) {
            if (hasCooldown(player)) return;

            NinjaEntry entry = ninjaEntryMap.get(player.getUniqueId());

            if (entry == null) return;

            Player target = entry.getTarget();

            if (entry.hasExpired() || target == null) {
                ninjaEntryMap.remove(player.getUniqueId());
                return;
            }

            if (target.getLocation().distance(player.getLocation()) > 25) {
                player.sendMessage("§cVocê está muito longe do alvo!");
                return;
            }

            player.teleport(target.getLocation());
            player.setFallDistance(0.0F);

            player.sendMessage("§eVocê foi teleportado até §b" + target.getName() + "§a!");

            addCooldown(player, getCooldown());
        }
    }
}
