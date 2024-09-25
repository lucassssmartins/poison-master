package br.com.poison.core.bukkit.event.list.profile.rank;

import br.com.poison.core.bukkit.event.list.profile.ProfileEvent;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.profile.resources.rank.Rank;
import lombok.Getter;

@Getter
public final class ProfileRankEvent extends ProfileEvent {

    private final Rank rank;

    public ProfileRankEvent(Profile profile, Rank rank) {
        super(profile);

        this.rank = rank;
    }
}
