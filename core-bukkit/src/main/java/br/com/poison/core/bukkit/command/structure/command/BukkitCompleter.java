package br.com.poison.core.bukkit.command.structure.command;

import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.command.inheritor.CommandInheritor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BukkitCompleter implements TabCompleter {

    private final Map<String, Map.Entry<Method, Object>> completers = new HashMap<>();

    public void addCompleter(String label, Method method, CommandInheritor commandInheritor) {
        completers.put(label, new AbstractMap.SimpleEntry<>(method, commandInheritor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        for (int i = args.length; i >= 0; i--) {
            StringBuilder builder = new StringBuilder();

            builder.append(label.toLowerCase());

            for (int y = 0; y < i; y++) {
                if (!args[y].isEmpty())
                    builder.append(".").append(args[y].toLowerCase());
            }

            String cmdLabel = builder.toString();

            if (completers.containsKey(cmdLabel)) {
                Map.Entry<Method, Object> entry = completers.get(cmdLabel);

                try {
                    return (List<String>) entry.getKey().invoke(entry.getValue(),
                            new BukkitCommandContext(sender, label, args, cmdLabel.split("\\.").length - 1));
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o completer...", e);
                }
            }
        }

        return null;
    }
}
