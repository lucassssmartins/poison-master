package br.com.poison.core.bukkit.command.structure.command;

import br.com.poison.core.Constant;
import br.com.poison.core.bukkit.command.structure.BukkitCommandLoader;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.BukkitCommandContext;
import br.com.poison.core.profile.Profile;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@Getter
@Setter
public class BukkitCommand extends org.bukkit.command.Command {

    private final JavaPlugin holder;
    private final CommandExecutor executor;

    private BukkitCompleter completer;
    private RankCategory rank;

    public BukkitCommand(JavaPlugin holder, RankCategory rank, String label) {
        super(label);

        this.holder = holder;
        this.executor = holder;

        this.rank = rank;
    }

    @SneakyThrows
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        List<String> completions = null;
        try {
            if (completer != null) {
                completions = completer.onTabComplete(sender, this, alias, args);
            }
            if (completions == null && executor instanceof TabCompleter) {
                completions = ((TabCompleter) executor).onTabComplete(sender, this, alias, args);
            }
        } catch (Throwable ex) {
            StringBuilder message = new StringBuilder();
            message.append("Unhandled exception during tab completion for command '/").append(alias).append(' ');

            for (String arg : args) {
                message.append(arg).append(' ');
            }

            message.deleteCharAt(message.length() - 1).append("' in plugin ")
                    .append(holder.getDescription().getFullName());

            throw new CommandException(message.toString(), ex);
        }

        if (completions == null) {
            return super.tabComplete(sender, alias, args);
        }
        return completions;
    }

    public boolean handleCommand(CommandSender sender, String label, String[] args) {
        for (int index = args.length; index >= 0; index--) {
            StringBuilder builder = new StringBuilder();

            builder.append(label.toLowerCase());

            for (int x = 0; x < index; x++) {
                builder.append(".").append(args[x].toLowerCase());
            }

            String cmdLabel = builder.toString();

            if (BukkitCommandLoader.getCommandMap().containsKey(cmdLabel)) {
                Map.Entry<Method, Object> entry = BukkitCommandLoader.getCommandMap().get(cmdLabel);

                Command command = entry.getKey().getAnnotation(Command.class);
                if (command == null) return false;

                boolean isPlayer = sender instanceof Player;

                if (command.expectedArguments() > -1 && args.length <= command.expectedArguments()) {
                    sender.sendMessage("§cUso: /" + cmdLabel + " " + command.usage());
                    return false;
                }

                if (!isPlayer && command.onlyPlayer()) {
                    sender.sendMessage("§cO comando só pode ser executado por jogadores.");
                    return false;
                }

                if (isPlayer) {
                    Profile account = Core.getProfileManager().read(((Player) sender).getUniqueId());
                    if (account == null) return false;

                    if (!account.hasRank(command.rank()) || !command.permission().isEmpty() && !account.hasPermission(command.permission())) {
                        account.sendMessage(Constant.WITHOUT_PERMISSION);
                        return false;
                    }
                }

                /* Rodar comando */
                try {
                    if (command.runAsync()) {
                        CompletableFuture.runAsync(() -> registerCommand(entry, sender, args,
                                label.replace(".", ""), cmdLabel.split("\\.").length - 1));
                    } else {
                        registerCommand(entry, sender, args,
                                label.replace(".", ""), cmdLabel.split("\\.").length - 1);
                    }
                } catch (Exception e) {
                    sender.sendMessage("§cOcorreu um erro ao tentar executar este comando!");

                    Core.getLogger().log(Level.WARNING, "Não foi possível executar o comando " + command.name(), e);
                }

                return true;
            }
        }

        return false;
    }

    private void registerCommand(Map.Entry<Method, Object> entry, CommandSender sender, String[] args, String label, int subCommand) {
        try {
            entry.getKey().invoke(entry.getValue(), new BukkitCommandContext(sender, label, args, subCommand));
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao registrar o comando...", e);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        boolean success;

        if (!holder.isEnabled()) return false;

        try {
            success = handleCommand(sender, label, args);
        } catch (Throwable exception) {
            sender.sendMessage("§cOcorreu um erro ao tentar executar este comando!");

            Core.getLogger().log(Level.WARNING, "Exceção não tratada executando comando '" + label + "' no plugin " + holder.getDescription().getFullName(), exception);
            return false;
        }

        return success;
    }

}
