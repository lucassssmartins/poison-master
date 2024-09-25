package br.com.poison.core.bukkit.command.structure;

import br.com.poison.core.command.context.CommandContext;
import br.com.poison.core.Core;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitCommandContext extends CommandContext {

    public BukkitCommandContext(CommandSender sender, String label, String[] args, int subCommand) {
        super(new BukkitCommandSender(sender), label, args, subCommand);
    }

    public List<String> getSubCommandsOf(String source) {
        return new ArrayList<>(BukkitCommandLoader.getCommandMap().keySet()).stream()
                .filter(command -> command.startsWith(source) && command.split("\\.").length >= 2)
                .map(command -> command.split("\\.")[1])
                .sorted(Comparator.comparing(String::toLowerCase))
                .collect(Collectors.toList());
    }

    public Player getPlayer() {
        return Core.getMultiService().getPlayer(getSender().getUuid(), Player.class);
    }

    public Player getPlayer(UUID uniqueId) {
        return Core.getMultiService().getPlayer(uniqueId, Player.class);
    }

    public Player getPlayer(String name) {
        return Core.getMultiService().getPlayer(name, Player.class);
    }
}
