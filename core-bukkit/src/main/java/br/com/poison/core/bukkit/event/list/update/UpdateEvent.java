package br.com.poison.core.bukkit.event.list.update;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.bukkit.event.list.update.type.UpdateType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdateEvent extends EventHandler {

    private final UpdateType type;

    private final long ticks;

    public boolean isType(UpdateType type) {
        return this.type.equals(type);
    }

    public int getSecond() {
        return (int) ticks / 20;
    }
}
