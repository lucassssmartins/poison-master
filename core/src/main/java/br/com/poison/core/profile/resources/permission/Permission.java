package br.com.poison.core.profile.resources.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Permission {

    private final String key;
    private final UUID author;

    private final long expiresAt, assignedAt = System.currentTimeMillis();

    public boolean isPermanent() {
        return expiresAt == -1L;
    }

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
