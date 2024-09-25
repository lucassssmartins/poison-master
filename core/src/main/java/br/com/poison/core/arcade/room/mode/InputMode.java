package br.com.poison.core.arcade.room.mode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InputMode {

    PLAYER("Jogador"),
    SPECTATOR("Espectador"),
    VANISHER("Moderador");

    private final String info;
}
