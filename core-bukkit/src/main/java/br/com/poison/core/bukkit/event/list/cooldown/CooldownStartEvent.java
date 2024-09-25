package br.com.poison.core.bukkit.event.list.cooldown;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@RequiredArgsConstructor
public class CooldownStartEvent extends EventHandler implements Cancellable {

    private final Player player;

    private final Cooldown cooldown;

    @Setter
    private boolean cancelled;
}
