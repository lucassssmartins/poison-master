package br.com.poison.core.bukkit.service.server;

import br.com.poison.core.manager.ServerManager;
import org.bukkit.Bukkit;

public class BukkitServer extends ServerManager {

    @Override
    public int port() {
        return Bukkit.getPort();
    }
}
