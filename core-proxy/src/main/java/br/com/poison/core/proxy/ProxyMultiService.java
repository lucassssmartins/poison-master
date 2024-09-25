package br.com.poison.core.proxy;

import br.com.poison.core.Constant;
import br.com.poison.core.MultiService;
import br.com.poison.core.util.Util;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RequiredArgsConstructor
public final class ProxyMultiService implements MultiService {

    private final ProxyCore proxy;

    @Override
    public void async(Runnable runnable) {
        proxy.getProxy().getScheduler().runAsync(proxy, runnable);
    }

    @Override
    public void sync(Runnable runnable) {
    }

    @Override
    public void syncLater(Runnable runnable, long ticks) {
    }

    @Override
    public <T> T getPlayer(UUID id, Class<T> tClass) {
        ProxiedPlayer player = proxy.getProxy().getPlayer(id);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public <T> T getPlayer(String name, Class<T> tClass) {
        ProxiedPlayer player = proxy.getProxy().getPlayer(name);
        return player != null ? tClass.cast(player) : null;
    }

    @Override
    public void broadcast(String message, boolean prefix) {
        proxy.getProxy()
                .getPlayers()
                .forEach(player -> player.sendMessage(TextComponent.fromLegacyText(
                        (prefix ? Constant.SERVER_PREFIX : "") + Util.color(message)
                )));
    }

    @Override
    public void broadcast(String message) {
        broadcast(message, true);
    }

    @Override
    public void sendMessage(UUID id, String message) {
        ProxiedPlayer player = getPlayer(id, ProxiedPlayer.class);

        if (player != null)
            player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(UUID id, String... messages) {
        ProxiedPlayer player = getPlayer(id, ProxiedPlayer.class);

        if (player != null)
            for (String message : messages)
                player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(UUID id, BaseComponent message) {
        ProxiedPlayer player = getPlayer(id, ProxiedPlayer.class);

        if (player != null)
            player.sendMessage(message);
    }

    @Override
    public void sendMessage(UUID id, BaseComponent... message) {
        ProxiedPlayer player = getPlayer(id, ProxiedPlayer.class);

        if (player != null)
            player.sendMessage(message);
    }
}
