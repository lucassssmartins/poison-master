package br.com.poison.core.bukkit.event.list.profile.clan.list;

import br.com.poison.core.bukkit.event.list.profile.clan.ProfileClanEvent;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.clan.Clan;

public final class ProfileClanEnterEvent extends ProfileClanEvent {
    public ProfileClanEnterEvent(Profile profile, Clan clan) {
        super(profile, clan);
    }
}
