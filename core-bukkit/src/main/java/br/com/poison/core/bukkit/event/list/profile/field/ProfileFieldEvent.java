package br.com.poison.core.bukkit.event.list.profile.field;

import br.com.poison.core.bukkit.event.list.profile.ProfileEvent;
import br.com.poison.core.profile.Profile;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
public final class ProfileFieldEvent extends ProfileEvent {

    private final Field field;

    private final Object old, value;

    public ProfileFieldEvent(Profile profile, Field field, Object old, Object value) {
        super(profile);

        this.field = field;

        this.old = old;
        this.value = value;
    }
}

