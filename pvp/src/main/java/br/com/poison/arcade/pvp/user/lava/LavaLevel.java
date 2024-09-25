package br.com.poison.arcade.pvp.user.lava;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum LavaLevel {

    EASY("Fácil", ChatColor.GREEN),
    MEDIUM("Médio", ChatColor.YELLOW),
    HARD("Difícil", ChatColor.RED),
    EXTREME("Extremo", ChatColor.DARK_RED);

    private final String name;

    private final ChatColor color;

    public String getColoredName() {
        return color + name;
    }

    public String getPrefix() {
        return color + "§l" + name.toUpperCase();
    }
}