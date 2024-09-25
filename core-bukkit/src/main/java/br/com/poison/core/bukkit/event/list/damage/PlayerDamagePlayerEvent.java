package br.com.poison.core.bukkit.event.list.damage;

import br.com.poison.core.bukkit.event.EventHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

@Getter
@AllArgsConstructor
public class PlayerDamagePlayerEvent extends EventHandler implements Cancellable {

    private final Player player, damager;

    @Setter
    private double damage;

    private final double finalDamage;

    private final AttackType type;

    @Setter
    private boolean cancelled;

    public enum AttackType {
        PROJECTILE, ATTACK
    }
}
