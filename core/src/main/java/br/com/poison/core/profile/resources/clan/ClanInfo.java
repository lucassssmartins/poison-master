package br.com.poison.core.profile.resources.clan;

import br.com.poison.core.Constant;
import br.com.poison.core.profile.resources.clan.exhibition.ClanExhibition;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClanInfo {

    private UUID clanId = Constant.CONSOLE_UUID;
    private UUID lastClanId = Constant.CONSOLE_UUID;

    private ClanExhibition exhibition = ClanExhibition.UNUSED;

    private long updatedAt = System.currentTimeMillis();

    public void update(UUID clanId) {
        this.lastClanId = getClanId();
        this.clanId = clanId;

        this.updatedAt = System.currentTimeMillis();
    }
}
