package br.com.poison.core.bukkit.event.list.cooldown;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.bukkit.api.user.cooldown.entities.Cooldown;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
@AllArgsConstructor
public class CooldownEndEvent extends EventHandler {

    private final Player player;

    private final Cooldown cooldown;
}
