package br.com.poison.core.resources.clan.rank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ClanRank {

    INITIAL("Initial", ChatColor.DARK_GRAY, "-", 0),
    CHIEF("Chefe", ChatColor.DARK_RED, "❇", 100000);

    private final String name;
    private final ChatColor color;

    private final String symbol;

    private final int minElo;

    public static ClanRank fetch(String name) {
        return Arrays.stream(values())
                .filter(rank -> rank.name().equalsIgnoreCase(name) || rank.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getColoredName() {
        return color + name;
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getPrefix() {
        return getColoredSymbol() + " " + name;
    }
}
