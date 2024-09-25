package br.com.poison.core.proxy.event.list.profile.skin;

import br.com.poison.core.resources.skin.Skin;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.proxy.event.list.profile.ProfileEvent;
import lombok.Getter;

@Getter
public class ProfileSkinChangeEvent extends ProfileEvent {

    private final Skin skin;

    public ProfileSkinChangeEvent(Profile profile, Skin skin) {
        super(profile);

        this.skin = skin;
    }
}
