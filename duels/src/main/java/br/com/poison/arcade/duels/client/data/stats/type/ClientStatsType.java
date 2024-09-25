package br.com.poison.arcade.duels.client.data.stats.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum ClientStatsType {

    PLAYER("Jogador", ChatColor.GRAY, -1),
    DIED("Morto", ChatColor.RED,-1),
    RESURFACING("Ressurgindo", ChatColor.DARK_GRAY, 5),
    VANISH("Vanish", ChatColor.LIGHT_PURPLE, -1),
    SPECTATOR("Espectador", ChatColor.YELLOW, -1);

    private final String name;
    private final ChatColor color;

    private final int duration;

    public String getColoredName() {
        return color + name;
    }

    public String getPrefix() {
        return color + "[" + name.toUpperCase() + "]";
    }
}
