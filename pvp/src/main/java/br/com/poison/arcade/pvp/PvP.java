package br.com.poison.arcade.pvp;

import br.com.poison.arcade.pvp.manager.UserManager;
import br.com.poison.arcade.pvp.manager.GameManager;
import br.com.poison.arcade.pvp.manager.KitManager;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.bukkit.listener.loader.ListenerLoader;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.bukkit.slime.api.SlimeWorldAPI;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Getter;

public class PvP extends BukkitCore {

    @Getter
    protected static UserManager userManager;

    @Getter
    protected static GameManager gameManager;

    @Getter
    protected static KitManager kitManager;

    @Override
    public void onLoad() {
        super.onLoad();

        saveDefaultConfig();

        ServerOptions.DAMAGE_ENABLED = true;
        ServerOptions.DROP_ITEM_ENABLED = true;
        ServerOptions.DEFAULT_CHAT = false;

        userManager = new UserManager();

        gameManager = new GameManager(this);
        kitManager = new KitManager(this);

        Core.setServerCategory(ServerCategory.PVP);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Slime
        SlimeWorldAPI.registerHikari("u23_WYEZHtEBaL", "slime_pvp", "ekki1YIevsJ1pvbCXtxn0=DR");

        gameManager.load();
        kitManager.load();

        new BukkitCommandLoader(this).register("br.com.poison.arcade.pvp.command");
        new ListenerLoader(this).register("br.com.poison.arcade.pvp");

        Core.getLogger().info("PvP iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        gameManager.unload();

        Core.getLogger().info("PvP encerrado com sucesso.");
    }
}
