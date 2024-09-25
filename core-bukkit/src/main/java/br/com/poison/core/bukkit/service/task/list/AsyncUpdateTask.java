package br.com.poison.core.bukkit.service.task.list;

import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.AsyncUpdateEvent;
import br.com.poison.core.bukkit.service.task.TaskHandler;
import org.bukkit.plugin.Plugin;

public class AsyncUpdateTask extends TaskHandler {

    private long ticks;

    public AsyncUpdateTask(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(getPlugin(), this, 0L, 1L);
    }

    @Override
    public void run() {
        ticks++;

        UpdateType type = ticks % 20 == 0 ? UpdateType.SECOND
                : ticks % 1200 == 0 ? UpdateType.MINUTE
                : ticks % 72000 == 0 ? UpdateType.HOUR : UpdateType.TICK;

        new AsyncUpdateEvent(type, ticks).call();
    }
}
