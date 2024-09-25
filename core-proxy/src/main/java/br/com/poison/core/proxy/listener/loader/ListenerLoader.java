package br.com.poison.core.proxy.listener.loader;

import br.com.poison.core.util.Util;
import br.com.poison.core.util.loader.ClassLoader;
import br.com.poison.core.Core;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.lang.reflect.Method;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListenerLoader {

    private final Plugin plugin;

    public void register(String path) {
        int success = 0;
        long startTime = System.currentTimeMillis();

        Core.getLogger().info("Registrando eventos...");

        for (Class<?> listenerClass : ClassLoader.getClassesForPackage(plugin, path)) {
            if (Listener.class.isAssignableFrom(listenerClass)) {
                try {
                    Listener listener = (Listener) listenerClass.newInstance();

                    plugin.getProxy().getPluginManager().registerListener(plugin, listener);

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
            Core.getLogger().info("Registro de eventos concluído com sucesso. " +
                    "(Total de eventos registrados: " + success + " - Tempo médio: " + Util.formatMS(startTime) + "ms)");
    }
}
