package br.com.poison.core.resources.clan.invitation;

import br.com.poison.core.util.extra.TimeUtil;
import br.com.poison.core.profile.Profile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ClanInvitation {

    private final UUID clanId;
    private final Profile inviting, guest;

    private final long expiresAt = TimeUtil.getTime("15m");

    public boolean hasExpired() {
        return expiresAt <= System.currentTimeMillis();
    }
}
