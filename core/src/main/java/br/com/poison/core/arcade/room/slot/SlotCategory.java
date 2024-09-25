package br.com.poison.core.arcade.room.slot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SlotCategory {

    VOID("Void", "0v0", 0),

    SOLO("Solo", "1v1", 2),
    PAIR("Dupla", "2v2", 4),
    TRIO("Trio", "3v3", 6),
    QUARTET("Quarteto", "4v4", 8);

    private final String name, tag;

    private final int totalPlayers;
}
