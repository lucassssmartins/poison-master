package br.com.poison.core.bukkit.event.list.profile.clan;

import br.com.poison.core.bukkit.event.EventHandler;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.clan.Clan;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileClanEvent extends EventHandler {
    private final Profile profile;
    private final Clan clan;
}
