package br.com.poison.arcade.pvp.kit.type.primary.ninja;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class NinjaEntry {

    private Player target;
    private long expiresAt;

    public NinjaEntry(Player target) {
        this.target = target;

        this.expiresAt = System.currentTimeMillis() + 10000L;
    }

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
