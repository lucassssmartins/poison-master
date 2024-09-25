package br.com.poison.core.bukkit.service;

import br.com.poison.core.Constant;
import br.com.poison.core.MultiService;
import br.com.poison.core.bukkit.BukkitCore;
import br.com.poison.core.util.Util;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.UUID;

@RequiredArgsConstructor
public final class BukkitMultiService implements MultiService {

    private final BukkitCore core;

    @Override
    public void async(Runnable runnable) {
        core.getServer().getScheduler().runTaskAsynchronously(core, runnable);
    }

    @Override
    public void sync(Runnable runnable) {
        core.getServer().getScheduler().runTask(core, runnable);
    }

    @Override
    public void syncLater(Runnable runnable, long ticks) {
        core.getServer().getScheduler().runTaskLater(core, runnable, ticks);
    }

    @Override
    public <T> T getPlayer(UUID id, Class<T> tClass) {
        Player player = core.getServer().getPlayer(id);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public <T> T getPlayer(String name, Class<T> tClass) {
        Player player = core.getServer().getPlayer(name);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public void broadcast(String message, boolean prefix) {
        core.getServer().getOnlinePlayers().forEach(player ->
                player.sendMessage((prefix ? Constant.SERVER_PREFIX : "") + Util.color(message)));
    }

    @Override
    public void broadcast(String message) {
        broadcast(message, true);
    }

    @Override
    public void sendMessage(UUID id, String message) {
        Player player = getPlayer(id, Player.class);

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID id, String... message) {
        Player player = getPlayer(id, Player.class);

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID id, BaseComponent message) {
        Player player = getPlayer(id, Player.class);

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID id, BaseComponent... message) {
        Player player = getPlayer(id, Player.class);

        if (player != null)
            player.sendMessage(message);
    }
}
