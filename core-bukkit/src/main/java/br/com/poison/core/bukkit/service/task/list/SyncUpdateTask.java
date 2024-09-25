package br.com.poison.core.bukkit.service.task.list;

import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import br.com.poison.core.bukkit.event.list.update.type.list.SyncUpdateEvent;
import br.com.poison.core.bukkit.service.task.TaskHandler;
import org.bukkit.plugin.Plugin;

public class SyncUpdateTask extends TaskHandler {

    private long ticks;

    public SyncUpdateTask(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        getPlugin().getServer().getScheduler().runTaskTimer(getPlugin(), this, 0L, 1L);
    }

    @Override
    public void run() {
        ticks++;

        UpdateType type = ticks % 20 == 0 ? UpdateType.SECOND
                : ticks % 1200 == 0 ? UpdateType.MINUTE
                : ticks % 72000 == 0 ? UpdateType.HOUR : UpdateType.TICK;

        new SyncUpdateEvent(type, ticks).call();
    }
}
