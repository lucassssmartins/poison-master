package br.com.poison.core.resources.member.type.pvp;

import br.com.poison.core.resources.member.Member;
import br.com.poison.core.resources.member.type.pvp.stats.PvPStats;
import br.com.poison.core.Core;
import br.com.poison.core.profile.Profile;
import lombok.Getter;

@Getter
public class PvPMember extends Member {

    protected transient final Profile profile;

    private PvPStats stats;

    public PvPMember(Profile profile) {
        super(profile);

        this.profile = profile;
        this.stats = new PvPStats();
    }

    @Override
    protected synchronized void save(String... fields) {
        for (String field : fields)
            Core.getPvpData().update(this, field);
    }

    public void saveStats(PvPStats stats) {
        this.stats = stats;
        save("type");
    }
}
