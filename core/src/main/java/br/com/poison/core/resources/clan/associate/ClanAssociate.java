package br.com.poison.core.resources.clan.associate;

import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.clan.office.ClanOffice;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanAssociate {

    private final UUID id;
    private final String name;

    private ClanOffice office;

    private final long joinedAt;

    private int eloWon;

    public ClanAssociate(Profile profile, ClanOffice office) {
        this.id = profile.getId();
        this.name = profile.getName();

        this.office = office;

        this.joinedAt = System.currentTimeMillis();
    }

    public boolean isOnline() {
        Profile profile = Core.getProfileData().read(id, false);

        return profile != null && profile.isOnline();
    }
}
