package br.com.poison.lobby.listener;

import br.com.poison.lobby.Lobby;
import br.com.poison.lobby.manager.GameManager;
import br.com.poison.core.Core;
import br.com.poison.core.arcade.category.ArcadeCategory;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.api.mechanics.hologram.type.server.HologramServer;
import br.com.poison.core.bukkit.event.list.update.type.list.AsyncUpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.manager.HologramManager;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;

public class HologramListener implements Listener {

    private final HologramManager hologramManager;
    private final GameManager gameManager;

    public HologramListener() {
        hologramManager = BukkitCore.getHologramManager();

        gameManager = Lobby.getGameManager();
    }

    @EventHandler
    public void asyncUpdate(AsyncUpdateEvent event) {
        if (event.isType(UpdateType.SECOND)) {
            // Atualizando hologramas dos modos de jogo.
            for (ServerCategory category : ServerCategory.values()) {
                List<HologramServer> servers = hologramManager.getServers(category.getName());

                if (servers == null || servers.isEmpty()) continue;

                for (HologramServer server : servers) {
                    if (server == null) continue;

                    int players = Core.getServerManager().getTotalPlayers(category) + gameManager.getTotalPlayers(category);

                    server.setText(category.equals(ServerCategory.DUELS) ? 2 : 1, "§e" + Util.formatNumber(players) + " jogando.");
                }
            }

            for (ArcadeCategory category : ArcadeCategory.values()) {
                if (category.getServer().equals(ServerCategory.LOBBY)) continue;

                List<HologramServer> servers = hologramManager.getServers(category.getName());

                if (servers == null || servers.isEmpty()) continue;

                for (HologramServer server : servers) {
                    if (server == null) continue;

                    server.setText(1, "§e" + category.getPlayingNow() + " jogando.");
                }
            }
        }
    }
}
