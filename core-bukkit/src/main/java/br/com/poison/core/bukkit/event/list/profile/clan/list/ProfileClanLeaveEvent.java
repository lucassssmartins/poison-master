package br.com.poison.core.bukkit.event.list.profile.clan.list;

import br.com.poison.core.bukkit.event.list.profile.clan.ProfileClanEvent;
import br.com.poison.core.profile.Profile;

public final class ProfileClanLeaveEvent extends ProfileClanEvent {
    public ProfileClanLeaveEvent(Profile profile) {
        super(profile, null);
    }
}
