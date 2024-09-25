package br.com.poison.core.bukkit.api.mechanics.inventory.sound;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Sound;

@Getter
@AllArgsConstructor
public enum SoundCategory {

    SUCCESS(Sound.SUCCESSFUL_HIT),
    WRONG(Sound.ITEM_BREAK),
    CHANGE(Sound.WOOD_CLICK);

    private final Sound sound;
}
