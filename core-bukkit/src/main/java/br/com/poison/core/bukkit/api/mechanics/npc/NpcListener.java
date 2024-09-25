package br.com.poison.core.bukkit.api.mechanics.npc;

import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.manager.NpcManager;
import br.com.poison.core.bukkit.api.mechanics.npc.action.Action;
import br.com.poison.core.bukkit.api.mechanics.npc.action.NpcAction;
import br.com.poison.core.bukkit.api.mechanics.npc.type.client.NpcClient;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class NpcListener implements Listener {

    private final NpcManager manager;

    public NpcListener() {
        manager = BukkitCore.getNpcManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null)
                    updatePlayer(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinedPlayer(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuitedPlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        for (Npc npc : manager.getNPCs()) {
            if (!npc.isSpawned()) continue;

            npc.getViewers().remove(player);

            if (npc instanceof NpcClient) {
                NpcClient client = (NpcClient) npc;

                if (client.getReceiver().equals(player))
                    client.destroy();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractedPlayer(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof LivingEntity) {
            LivingEntity clicked = (LivingEntity) event.getRightClicked();

            Npc npc = manager.getNPC(clicked.getEntityId());

            if (npc != null) {
                event.setCancelled(true);

                NpcAction action = npc.getAction();

                if (action != null)
                    action.handleAction(event.getPlayer(), Action.RIGHT);
            }
        }
    }

    private void updatePlayer(Player viewer) {
        for (Npc npc : manager.getNPCs()) {
            if (!npc.isSpawned()) continue;

            if (!viewer.inWorld(npc.getWorld()) || (npc instanceof NpcClient && !((NpcClient) npc).getReceiver().equals(viewer)))
                npc.getViewers().remove(viewer);
            else if (!manager.canSpawn(viewer.getLocation(), npc.getLocation())) {
                npc.despawnTo(viewer);
            } else
                npc.spawnTo(viewer);
        }
    }
}