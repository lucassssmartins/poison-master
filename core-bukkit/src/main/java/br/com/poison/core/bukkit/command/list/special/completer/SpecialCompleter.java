package br.com.poison.core.bukkit.command.list.special.completer;

import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.annotation.Completer;
import br.com.poison.core.command.inheritor.CommandInheritor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpecialCompleter implements CommandInheritor {

    @Completer(name = "clan")
    public List<String> clanCompleter(BukkitCommandContext context) {
        List<String> completers = new ArrayList<>();

        List<String> suggestions = context.getSubCommandsOf("clan");

        String[] args = context.getArgs();

        if (args.length > 0 && !args[0].isEmpty()) {
            String filter = args[0].toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

            for (String suggestion : suggestions) {
                if (suggestion.startsWith(filter))
                    completers.add(suggestion);
            }
        } else {
            completers.addAll(suggestions);
        }

        if (args.length > 1)
            return getPlayerList(args, 1);

        return completers;
    }

    protected List<String> getPlayerList(String[] args, int index) {
        List<String> playerList = new ArrayList<>();

        List<String> names = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());

        if (args.length > index && !args[index].isEmpty()) {
            String filter = args[index].toLowerCase(); // Para tornar a comparação de string insensível a maiúsculas e minúsculas

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
