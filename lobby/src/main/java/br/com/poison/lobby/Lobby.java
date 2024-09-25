package br.com.poison.lobby;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.bukkit.listener.loader.ListenerLoader;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.lobby.manager.GameManager;
import br.com.poison.lobby.manager.UserManager;
import lombok.Getter;

public class Lobby extends BukkitCore {

    @Getter
    protected static UserManager userManager;

    @Getter
    protected static GameManager gameManager;

    @Override
    public void onLoad() {
        super.onLoad();

        saveDefaultConfig();

        userManager = new UserManager();

        gameManager = new GameManager(this);

        Core.setServerCategory(ServerCategory.LOBBY);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        SlimeWorldAPI.registerHikari("", "slime_lobby", "");

        new BukkitCommandLoader(this).register("br.com.poison.lobby.command");
        new ListenerLoader(this).register("br.com.poison.lobby.listener");

        gameManager.load();

        Core.getLogger().info("Lobby iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        gameManager.unload();

        Core.getLogger().info("Lobby encerrado com sucesso.");
    }
}
