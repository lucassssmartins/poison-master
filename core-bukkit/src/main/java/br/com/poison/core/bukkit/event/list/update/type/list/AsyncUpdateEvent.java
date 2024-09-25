package br.com.poison.core.bukkit.event.list.update.type.list;

import br.com.poison.core.bukkit.event.list.update.UpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;

public class AsyncUpdateEvent extends UpdateEvent {
    public AsyncUpdateEvent(UpdateType type, long ticks) {
        super(type, ticks);
    }
}
