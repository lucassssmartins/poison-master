package br.com.poison.core.proxy.command.list.completer;

import br.com.poison.core.command.annotation.Completer;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.proxy.command.structure.ProxyCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CompleterCommands implements CommandInheritor {

    @Completer(name = "message", aliases = {"msg", "w", "whisper", "tell", "report", "rp", "denunciar", "go", "ban", "banir", "cban", "mute", "mutar", "silenciar", "pardon"})
    public List<String> playerName(ProxyCommandContext context) {
        return getPlayerList(context.getArgs());
    }

    protected List<String> getPlayerList(String[] args) {
        List<String> playerList = new ArrayList<>();

        List<String> names = ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList());

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

            for (String name : names) {
                if (name.startsWith(filter))
                    playerList.add(name);
            }
        } else {
            playerList.addAll(names);
        }

        return playerList;
    }
}
