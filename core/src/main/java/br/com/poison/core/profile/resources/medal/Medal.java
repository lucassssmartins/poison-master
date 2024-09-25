package br.com.poison.core.profile.resources.medal;

import br.com.poison.core.profile.resources.permission.category.PermissionCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Medal {

    VOID("Vázia", "", ChatColor.RESET, 0, false, false, "");

    private final String name, symbol;
    private final ChatColor color;

    private final int price;
    private final boolean reward, exclusive;

    private final String lore;

    public static Medal fetch(String name) {
        return Arrays.stream(values())
                .filter(medal -> medal.name().equalsIgnoreCase(name) || medal.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getPermission() {
        return String.format(PermissionCategory.MEDAL.getKey(), name.toLowerCase());
    }

    public String getColoredSymbol() {
        return color + symbol;
    }

    public String getColoredName() {
        return color + name;
    }

    public String getPrefix() {
        return getColoredSymbol() + " " + name;
    }
}
