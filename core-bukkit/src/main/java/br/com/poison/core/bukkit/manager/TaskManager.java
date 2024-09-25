package br.com.poison.core.bukkit.manager;

import br.com.poison.core.bukkit.service.task.TaskHandler;
import br.com.poison.core.util.loader.ClassLoader;
import br.com.poison.core.Core;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class TaskManager {

    @Getter
    private static final List<TaskHandler> tasks = new ArrayList<>();

    public static void registerTasks(Plugin plugin, String directory) {
        Instant startedAt = Instant.now();

        int loaded = 0;

        Core.getLogger().info("Registrando tasks...");

        for (Class<?> taskClass : ClassLoader.getClassesForPackage(plugin, directory)) {
            if (TaskHandler.class.isAssignableFrom(taskClass)) {
                try {
                    TaskHandler task = (TaskHandler) taskClass
                            .getConstructor(Plugin.class)
                            .newInstance(plugin);

                    task.init();
                    tasks.add(task);

                    loaded++;
                } catch (Exception e) {
                    Core.getLogger().log(Level.WARNING, "Não foi possível registrar a task " + taskClass.getSimpleName(), e);
                }
            }
        }

        if (loaded > 0)
            Core.getLogger().info("Registro de tasks concluído com sucesso. (Total de tasks registradas: " + tasks
                    + " em " + Duration.between(startedAt, Instant.now()).toMillis() + "ms)");
    }

    public static void unloadTasks() {
        tasks.clear();
        Bukkit.getScheduler().cancelAllTasks();
    }
}
