package br.com.poison.core.resources.invitation;

import br.com.poison.core.Core;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
public class Invitation {

    private final UUID sender, target;

    private final long createdAt = System.currentTimeMillis(), expiresAt;

    public Invitation(UUID sender, UUID target, int expireSeconds) {
        this.sender = sender;
        this.target = target;

        this.expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds);

        Core.getInvitationManager().save(this);
    }

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
