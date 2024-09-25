package br.com.poison.core.bukkit.event.list.update.type.list;

import br.com.poison.core.bukkit.event.list.update.UpdateEvent;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;

public class SyncUpdateEvent extends UpdateEvent {
    public SyncUpdateEvent(UpdateType type, long ticks) {
        super(type, ticks);
    }
}
