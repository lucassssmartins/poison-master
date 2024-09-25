package br.com.poison.core.profile.resources.rank.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public enum RankCategory {

    PLAYER("Membro", ChatColor.GRAY),

    /* VIP Categories */
    VENOM("Venom", ChatColor.GREEN),
    POISON("Poison", ChatColor.DARK_GREEN),
    BETA("Beta", ChatColor.DARK_BLUE),

    /* Media Categories */
    MEDIA("Midia", ChatColor.AQUA),
    PLUS_MEDIA("Midia+", ChatColor.DARK_AQUA),

    /* Staff Categories */
    BUILDER("Builder", ChatColor.GREEN),
    TRIAL("Trial", ChatColor.LIGHT_PURPLE),
    MOD("Mod", ChatColor.DARK_PURPLE),
    MODPLUS("Mod+", ChatColor.DARK_PURPLE),
    DEV("Dev", ChatColor.DARK_AQUA, Collections.singletonList("*")),
    OWNER("Dono", ChatColor.DARK_RED, Collections.singletonList("*"));

    private final String name;
    private final ChatColor color;

    private final List<String> permissions;

    RankCategory(String name, ChatColor color) {
        this(name, color, new ArrayList<>());
    }

    public static RankCategory fetch(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public boolean isVIP() {
        return ordinal() >= 1 && ordinal() <= 4;
    }

    public RankCategory bellow() {
        return values()[ordinal() - 1];
    }

    public String getPrefix() {
        return color + name;
    }
}
