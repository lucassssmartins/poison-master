package br.com.poison.core.bukkit.command.structure;

import br.com.poison.core.bukkit.command.structure.command.BukkitCompleter;
import br.com.poison.core.Core;
import br.com.poison.core.bukkit.command.structure.command.BukkitCommand;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.loader.ClassLoader;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.command.context.CommandContext;
import br.com.poison.core.command.loader.CommandLoader;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.annotation.Completer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RequiredArgsConstructor
public final class BukkitCommandLoader implements CommandLoader {

    private final JavaPlugin plugin;

    private final CommandMap map;

    @Getter
    private static final Map<String, Map.Entry<Method, Object>> commandMap = new HashMap<>();

    private Map<String, org.bukkit.command.Command> knownCommands;

    public BukkitCommandLoader(JavaPlugin plugin) {
        this.plugin = plugin;

        this.map = plugin.getServer().getCommandMap();

        try {
            Field field = map.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);

            knownCommands = (HashMap<String, org.bukkit.command.Command>) field.get(map);
        } catch (Exception e) {
            Core.getLogger().log(Level.WARNING, "Ocorreu um erro ao carregar os comandos", e);
        }
    }

    @Override
    public void initClass(CommandInheritor commandInheritor) {
        for (Method method : commandInheritor.getClass().getMethods()) {

            if (method.isAnnotationPresent(Command.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().info("Não foi possível registrar o comando " + method.getName() + ". Argumentos de método não esperados!");
                    continue;
                }

                Command command = method.getAnnotation(Command.class);
                if (command == null) break;

                for (String alias : command.aliases()) {
                    registerCommand(commandInheritor, command, alias, method);
                }

                registerCommand(commandInheritor, command, command.name(), method);
            }

            if (method.isAnnotationPresent(Completer.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().info("Não foi possível registrar o completer " + method.getName() + ". Argumentos de método não esperados!");
                    continue;
                }

                if (method.getReturnType() != List.class) {
                    Core.getLogger().info("Tipo de retorno inesperado. O retorno deve ser uma Lista de Strings.");
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);
                if (completer == null) break;

                for (String alias : completer.aliases()) {
                    registerCompleter(commandInheritor, method, alias);
                }

                registerCompleter(commandInheritor, method, completer.name());
            }
        }
    }

    @Override
    public void register(String path) {
        Core.getLogger().info("Registrando comandos...");

        long start = System.currentTimeMillis();

        int loaded = 0;
        for (Class<?> commandClass : ClassLoader.getClassesForPackage(plugin, path)) {
            if (CommandInheritor.class.isAssignableFrom(commandClass)) {
                try {
                    CommandInheritor command = (CommandInheritor) commandClass.newInstance();

                    initClass(command);
                    loaded++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o comando " + commandClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("Registro de comandos concluído com sucesso. (" + loaded + "/" + Util.formatMS(start) + ")");
    }

    private void registerCommand(CommandInheritor base, Command command, String label, Method method) {
        Map.Entry<Method, Object> entry = new AbstractMap.SimpleEntry<>(method, base);

        commandMap.put(label.toLowerCase(), entry);

        String commandLabel = label.split("\\.")[0].toLowerCase();

        if (map.getCommand(commandLabel) == null) {
            org.bukkit.command.Command cmd = new BukkitCommand(plugin, command.rank(), commandLabel);

            cmd.setUsage(command.usage());

            map.register(commandLabel, cmd);
        } else if (map.getCommand(commandLabel) instanceof BukkitCommand) {
            BukkitCommand bukkitCommand = (BukkitCommand) map.getCommand(commandLabel);

            if (bukkitCommand != null) {
                bukkitCommand.setUsage(command.usage());
                bukkitCommand.setRank(command.rank());
            }
        }
    }

    private void registerCompleter(CommandInheritor base, Method method, String label) {
        String cmdLabel = label.split("\\.")[0].toLowerCase();

        if (map.getCommand(cmdLabel) == null) {
            org.bukkit.command.Command command = new BukkitCommand(plugin, RankCategory.PLAYER, cmdLabel);

            knownCommands.put(cmdLabel, command);
        }

        if (map.getCommand(cmdLabel) instanceof BukkitCommand) {
            BukkitCommand command = (BukkitCommand) map.getCommand(cmdLabel);

            if (command.getCompleter() == null) {
                command.setCompleter(new BukkitCompleter());
            }

            command.getCompleter().addCompleter(label, method, base);
        }

        if (map.getCommand(cmdLabel) instanceof PluginCommand) {
            try {
                org.bukkit.command.Command command = map.getCommand(cmdLabel);

                Field field = command.getClass().getDeclaredField("completer");
                field.setAccessible(true);

                if (field.get(command) == null) {
                    BukkitCompleter completer = new BukkitCompleter();

                    completer.addCompleter(label, method, base);
                    field.set(command, completer);
                }

                if (field.get(command) instanceof BukkitCompleter) {
                    BukkitCompleter completer = (BukkitCompleter) field.get(command);

                    completer.addCompleter(label, method, base);
                } else {
                    Core.getLogger().info("Não foi possível registrar o completer " + method.getName()
                            + ". Um completer já está registrado para esse comando!");
                }

            } catch (Exception e) {
                Core.getLogger().log(Level.WARNING, "Não foi possível registrar o completador...", e);
            }
        }
    }
}
