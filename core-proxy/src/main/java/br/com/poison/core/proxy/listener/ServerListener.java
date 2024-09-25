package br.com.poison.core.proxy.listener;

import br.com.poison.core.Constant;
import br.com.poison.core.server.Server;
import br.com.poison.core.server.category.ServerCategory;
import br.com.poison.core.util.extra.StringUtil;
import br.com.poison.core.Core;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Arrays;
import java.util.List;

public class ServerListener implements Listener {

    private final List<String> MOTD_DESCRIPTION = Arrays.asList(
            "§a§lVENHA APROVEITAR AS FÉRIAS!",
            "§6§lPRACTICE ABERTO PARA TODOS"
    );

    @EventHandler
    public void onDescription(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        String description = MOTD_DESCRIPTION.get(Core.RANDOM.nextInt(MOTD_DESCRIPTION.size()));

        ping.setDescriptionComponent(new TextComponent(
                // Header
                StringUtil.makeCenteredMotd("§e❃ " + Constant.SERVER_TITLE + " §7[1.7-1.16] §9" + Constant.DISCORD + " §e❃")
                        // Footer
                        + "\n" + StringUtil.makeCenteredMessage(description)
        ));

        event.setResponse(ping);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(PreLoginEvent event) {
        PendingConnection connection = event.getConnection();

        Server server = connection.isOnlineMode()
                ? Core.getServerManager().getServer(ServerCategory.LOBBY)
                : Core.getServerManager().getServer(ServerCategory.AUTH);

        if (server == null || server.getServerInfo() == null) {
            event.setCancelReason(TextComponent.fromLegacyText("§cNenhum servidor de " + (connection.isOnlineMode() ? "Lobby" : "Login") + " encontrado!"));
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onConnect(ServerConnectEvent event) {
        if (event.getReason().equals(ServerConnectEvent.Reason.JOIN_PROXY)) {
            ProxiedPlayer proxied = event.getPlayer();

            PendingConnection connection = proxied.getPendingConnection();

            Server server = connection.isOnlineMode()
                    ? Core.getServerManager().getServer(ServerCategory.LOBBY)
                    : Core.getServerManager().getServer(ServerCategory.AUTH);

            if (server == null || server.getServerInfo() == null) {
                proxied.disconnect(TextComponent.fromLegacyText("§cNenhum servidor de "
                        + (connection.isOnlineMode() ? "Lobby" : "Login") + " encontrado!"));

                event.setCancelled(true);
                return;
            }

            event.setTarget(server.getServerInfo());
        }
    }
}
