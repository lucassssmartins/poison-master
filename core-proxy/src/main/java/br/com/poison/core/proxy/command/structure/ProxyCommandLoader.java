package br.com.poison.core.proxy.command.structure;

import br.com.poison.core.Constant;
import br.com.poison.core.command.annotation.Command;
import br.com.poison.core.command.annotation.Completer;
import br.com.poison.core.command.context.CommandContext;
import br.com.poison.core.command.loader.CommandLoader;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.loader.ClassLoader;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.command.inheritor.CommandInheritor;
import br.com.poison.core.proxy.ProxyCore;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ProxyCommandLoader implements CommandLoader {

    private final ProxyCore core;

    private final Map<String, Map.Entry<Method, Object>> commands = new HashMap<>();

    public static final Map<String, Map.Entry<Method, Object>> completers = new HashMap<>();

    @Override
    public void initClass(CommandInheritor cmdClass) {
        for (Method method : cmdClass.getClass().getMethods()) {

            if (method.isAnnotationPresent(Command.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível registrar o comando " + method.getName() + ". Argumentos de métodos inesperados.");
                    continue;
                }

                Command command = method.getAnnotation(Command.class);

                if (command == null) break;

                for (String alias : command.aliases()) {
                    registerCommand(alias, method, cmdClass);
                }

                registerCommand(command.name(), method, cmdClass);
            } else if (method.isAnnotationPresent(Completer.class)) {

                if (method.getParameterCount() != 1 || !CommandContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível registrar o completer " + method.getName() + ". Argumentos de métodos inesperados.");
                    continue;
                }

                if (method.getReturnType() != List.class) {
                    Core.getLogger().log(Level.SEVERE, "Não foi possível registrar o completer " + method.getName() + ". Tipo de retorno inválido.");
                    continue;
                }

                Completer completer = method.getAnnotation(Completer.class);

                for (String alias : completer.aliases()) {
                    registerCompleter(alias, method, cmdClass);
                }

                registerCompleter(completer.name(), method, cmdClass);
            }
        }
    }

    @Override
    public void register(String path) {
        long startTime = System.currentTimeMillis();
        int success = 0;

        Core.getLogger().info("Registrando comandos...");

        for (Class<?> cmdClass : ClassLoader.getClassesForPackage(core, path)) {
            if (CommandInheritor.class.isAssignableFrom(cmdClass)) {
                try {
                    CommandInheritor command = (CommandInheritor) cmdClass.newInstance();

                    initClass(command);

                    success++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o comando " + cmdClass.getSimpleName() + "!", e);
                }
            }
        }

        if (success > 0)
            Core.getLogger().info("Registro de comandos concluído com sucesso. " +
                    "(Total de comandos registrados: " + Util.formatNumber(success) + " - Tempo médio: " + Util.formatMS(startTime) + "ms)");
    }

    protected void handleCommand(CommandSender sender, String label, String[] args) {
        for (int i = args.length; i >= 0; i--) {

            StringBuilder builder = new StringBuilder();

            builder.append(label.toLowerCase());

            for (int x = 0; x < i; x++) {
                builder.append(".").append(args[x].toLowerCase());
            }

            String commandLabel = builder.toString();

            if (commands.containsKey(commandLabel)) {

                Map.Entry<Method, Object> entry = commands.get(commandLabel);

                Command command = entry.getKey().getAnnotation(Command.class);

                boolean isPlayer = sender instanceof ProxiedPlayer;

                if (!isPlayer) {

                    if (command.onlyPlayer()) {
                        sender.sendMessage(TextComponent.fromLegacyText("§cSomente jogadores podem usar este comando."));
                        return;
                    }

                } else {

                    if (!command.rank().equals(RankCategory.PLAYER)) {
                        Profile profile = Core.getProfileManager().read(sender.getName());

                        if (profile == null || !profile.hasRank(command.rank())) {
                            sender.sendMessage(TextComponent.fromLegacyText(Constant.WITHOUT_PERMISSION));
                            return;
                        }
                    }
                }

                /* Register Bungee Command */

                if (command.runAsync()) {
                    Core.getMultiService().async(
                            () -> registerCommand(entry, sender, label.replace(".", " "), args, commandLabel.split("\\.").length - 1));
                } else {
                    registerCommand(entry, sender, label.replace(".", " "), args, commandLabel.split("\\.").length - 1);
                }

            }
        }
    }

    protected void registerCompleter(String label, Method method, Object object) {
        completers.put(label.toLowerCase(), new AbstractMap.SimpleEntry<>(method, object));
    }

    protected void registerCommand(Map.Entry<Method, Object> entry, CommandSender sender, String label, String[] args, int subCommand) {
        try {
            entry.getKey().invoke(entry.getValue(), new ProxyCommandContext(sender, label, args, subCommand));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void registerCommand(String label, Method method, Object object) {
        Map.Entry<Method, Object> entry = new AbstractMap.SimpleEntry<>(method, object);

        commands.put(label.toLowerCase(), entry);

        String commandLabel = label.replace(".", ",").split(",")[0].toLowerCase();

        core.getProxy().getPluginManager().registerCommand(core, new BungeeCommand(commandLabel));
    }

    class BungeeCommand extends net.md_5.bungee.api.plugin.Command {

        protected BungeeCommand(String label) {
            super(label);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            handleCommand(sender, getName(), args);
        }
    }

    public static class BungeeCompleter implements Listener {

        @SuppressWarnings("unchecked")
        @EventHandler
        public void onTabComplete(TabCompleteEvent event) {
            if (!(event.getSender() instanceof ProxiedPlayer))
                return;

            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String[] split = event.getCursor().replaceAll("\\s+", " ").split(" ");

            if (split.length == 0)
                return;

            String[] args = new String[split.length - 1];

            for (int i = 1; i < split.length; i++) {
                args[i - 1] = split[i];
            }

            String label = split[0].substring(1);

            for (int i = args.length; i >= 0; i--) {
                StringBuilder buffer = new StringBuilder();
                buffer.append(label.toLowerCase());

                for (int x = 0; x < i; x++) {
                    buffer.append(".").append(args[x].toLowerCase());
                }

                String cmdLabel = buffer.toString();

                if (completers.containsKey(cmdLabel)) {
                    Map.Entry<Method, Object> entry = completers.get(cmdLabel);
                    try {
                        event.getSuggestions().clear();

                        List<String> list = (List<String>) entry.getKey().invoke(entry.getValue(),
                                new ProxyCommandContext(player, label, args, cmdLabel.split("\\.").length - 1));

                        event.getSuggestions().addAll(list);
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
