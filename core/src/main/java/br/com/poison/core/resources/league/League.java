package br.com.poison.core.resources.league;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@Getter
@AllArgsConstructor
public enum League {

    INITIAL("Inicial", "-", ChatColor.DARK_GRAY, 0),
    LEGENDARY("Legendary", "", ChatColor.DARK_RED, 100000);

    private final String name, symbol;
    private final ChatColor color;

    private final int minimumXp;

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getSymbolPrefix() {
        return "§7[" + getColoredSymbol() + "§7]";
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPrefix() {
        return getColoredSymbol() + " " + name;
    }

    public boolean hasNext() {
        return ordinal() != LEGENDARY.ordinal();
    }

    public League next() {
        return this != LEGENDARY ? values()[ordinal() + 1] : null;
    }
}
