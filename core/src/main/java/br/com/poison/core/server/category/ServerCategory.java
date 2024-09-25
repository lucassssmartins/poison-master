package br.com.poison.core.server.category;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum ServerCategory {

    PROXY("Poison", "proxy"),
    AUTH("Auth", "auth"),
    LOBBY("Lobby", "lobby"),

    /* Arcade Categories */
    PVP("PvP", "pvp", true),
    DUELS("Duels", "duels", true);

    private final String name, id;

    private final boolean allowArcade;

    ServerCategory(String name, String id) {
        this(name, id, false);
    }

    public static ServerCategory fetch(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public String getId(int serverId) {
        return getId() + "-" + serverId;
    }
}
