package br.com.poison.core.bukkit.api.mechanics.hologram;

import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.client.HologramClient;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.manager.HologramManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HologramListener implements Listener {

    private final HologramManager manager;

    public HologramListener() {
        this.manager = BukkitCore.getHologramManager();
    }

    protected synchronized void updateHolograms() {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (hologram == null || hologram.getLocation() == null) continue;

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.inWorld(hologram.getWorld()))
                    hologram.getViewers().remove(player);

                if (hologram instanceof HologramClient) {
                    HologramClient client = (HologramClient) hologram;

                    if (client.getReceiver().equals(player) && manager.canSpawn(client, player.getLocation()))
                        client.spawnTo(player);
                    else
                        client.despawnTo(player);
                }

                if (hologram instanceof HologramServer) {
                    if (manager.canSpawn(hologram, player.getLocation()))
                        hologram.spawnTo(player);
                    else
                        hologram.despawnTo(player);
                }
            }
        }

    }

    protected synchronized void onExit(Player player) {
        for (Hologram hologram : manager.getHolograms().values()) {
            if (!(hologram instanceof HologramClient)) continue;

            HologramClient client = (HologramClient) hologram;

            if (client.getReceiver().equals(player))
                manager.removeClient(client);
        }
    }

    @EventHandler
    public void onUpdate(SyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            updateHolograms();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        onExit(event.getPlayer());
    }

    @EventHandler
    public void kick(PlayerKickEvent event) {
        onExit(event.getPlayer());
    }
}
