package br.com.poison.core.arcade.category;

import br.com.poison.core.arcade.ArcadeGame;
import br.com.poison.core.arcade.room.slot.SlotCategory;
import br.com.poison.core.Core;
import br.com.poison.core.server.category.ServerCategory;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ArcadeCategory {

    /* Hub Categories */
    HUB("Hub", ServerCategory.LOBBY),
    PVP("PvP", ServerCategory.LOBBY),
    DUELS("Duels", ServerCategory.LOBBY),

    /* PvP Categories */
    ARENA("Arena", ServerCategory.PVP),
    FPS("FPS", ServerCategory.PVP),
    LAVA("Lava", ServerCategory.PVP),

    /* Duels Categories */
    SIMULATOR("Simulator", "MUSHROOM_SOUP", ServerCategory.DUELS, true, false, SlotCategory.SOLO),
    GLADIATOR("Gladiator", "IRON_FENCE", ServerCategory.DUELS, true, false, SlotCategory.SOLO),
    SOUP("Sopa", "STONE_SWORD", ServerCategory.DUELS, SlotCategory.SOLO),
    NODEBUFF("NoDebuff", "POTION", ServerCategory.DUELS, SlotCategory.SOLO),
    UHC("UHC", "GOLDEN_APPLE", ServerCategory.DUELS, SlotCategory.SOLO),
    SUMO("Sumo", "APPLE", ServerCategory.DUELS, SlotCategory.SOLO);

    private final String name, icon;

    private final ServerCategory server;
    private final SlotCategory[] slots;

    private final boolean allowedLiquid, allowedDangerousBlocks;

    ArcadeCategory(String name, String icon, ServerCategory server, boolean allowedLiquid, boolean allowedDangerousBlocks, SlotCategory... slots) {
        this.name = name;
        this.icon = icon;

        this.server = server;
        this.slots = slots;

        this.allowedLiquid = allowedLiquid;
        this.allowedDangerousBlocks = allowedDangerousBlocks;
    }

    ArcadeCategory(String name, String icon, ServerCategory server, SlotCategory... slots) {
        this(name, icon, server, false, false, slots);
    }

    ArcadeCategory(String name, ServerCategory server) {
        this(name, "", server, false, false, SlotCategory.VOID);
    }

    public static ArcadeCategory fetch(String name) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(name) || category.name().replace("_", "").equalsIgnoreCase(name)
                        || category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public int getPlayingNow() {
        return Core.getRedisDatabase().getTotalCount(String.format(ArcadeGame.GAME_KEY,
                server.getName().toLowerCase(), name.toLowerCase()));
    }
}
