package br.com.poison.core.proxy.command.structure;

import br.com.poison.core.command.context.CommandContext;
import br.com.poison.core.Core;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class ProxyCommandContext extends CommandContext {

    public ProxyCommandContext(CommandSender sender, String label, String[] args, int subCommand) {
        super(new ProxyCommandSender(sender), label, args, subCommand);
    }

    public ProxiedPlayer getProxied() {
        return Core.getMultiService().getPlayer(getSender().getUuid(), ProxiedPlayer.class);
    }

    public ProxiedPlayer getProxied(UUID uuid) {
        return Core.getMultiService().getPlayer(uuid, ProxiedPlayer.class);
    }

    public ProxiedPlayer getProxied(String name) {
        return Core.getMultiService().getPlayer(name, ProxiedPlayer.class);
    }
}
