package br.com.poison.core.resources.clan.office;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ClanOffice {

    ASSOCIATE("Associado", ChatColor.GRAY),
    RECRUITER("Recrutador", ChatColor.GREEN),
    MOD("Mod", ChatColor.DARK_PURPLE),
    ADMIN("Admin", ChatColor.RED),
    OWNER("Dono", ChatColor.DARK_RED);

    private final String name;
    private final ChatColor color;

    public static ClanOffice fetch(String name) {
        return Arrays.stream(values())
                .filter(office -> office.name().equalsIgnoreCase(name) || office.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getPrefix() {
        return color + name;
    }
}
