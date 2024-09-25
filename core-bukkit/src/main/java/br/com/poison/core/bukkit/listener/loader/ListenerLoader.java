package br.com.poison.core.bukkit.listener.loader;

import br.com.poison.core.bukkit.listener.loader.ignore.IgnoreEvent;
import br.com.poison.core.Core;
import br.com.poison.core.util.Util;
import br.com.poison.core.util.loader.ClassLoader;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListenerLoader {

    private final JavaPlugin plugin;

    public void register(String path) {
        long startTime = System.currentTimeMillis();

        int success = 0;

        Core.getLogger().info("Registrando eventos...");

        for (Class<?> listenerClass : ClassLoader.getClassesForPackage(plugin, path)) {
            if (listenerClass.isAnnotationPresent(IgnoreEvent.class)) continue;

            if (Listener.class.isAssignableFrom(listenerClass)) {
                try {
                    Listener listener = (Listener) listenerClass.newInstance();

                    plugin.getServer().getPluginManager().registerEvents(listener, plugin);

                    for (Method method : listener.getClass().getMethods()) {
                        if (method.isAnnotationPresent(EventHandler.class))
                            success++;
                    }

                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar o evento " + listenerClass.getSimpleName(), e);
                }
            }
        }

        if (success > 0)
            Core.getLogger().info("Registro de eventos concluído com sucesso. (Total de eventos registrados: " + success + " - Tempo médio: " + Util.formatMS(startTime) + "ms)");
    }
}
