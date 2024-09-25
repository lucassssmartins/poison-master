package br.com.poison.auth;

import br.com.poison.auth.user.manager.UserManager;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.bukkit.listener.loader.ListenerLoader;
import br.com.poison.core.bukkit.service.server.options.ServerOptions;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Auth extends BukkitCore {

    @Getter
    protected static UserManager userManager;

    @Getter
    protected static Location spawn;

    @Override
    public void onLoad() {
        super.onLoad();

        ServerOptions.DEFAULT_CHAT = false;

        Core.setServerCategory(ServerCategory.AUTH);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        userManager = new UserManager();

        spawn = new Location(Bukkit.getWorlds().get(0), 100.527, 52.52937, 100.464, -1.7f, 0.2f);

        new BukkitCommandLoader(this).register("br.com.poison.auth.command");
        new ListenerLoader(this).register("br.com.poison.auth.listener");

        Core.getLogger().info("Auth iniciado com sucesso.");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Core.getLogger().info("Auth desligado com sucesso.");
    }
}
