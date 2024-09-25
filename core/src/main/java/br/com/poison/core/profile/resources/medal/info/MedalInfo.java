package br.com.poison.core.profile.resources.medal.info;

import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import br.com.poison.core.profile.resources.medal.Medal;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MedalInfo {

    private final Medal medal;
    private final UUID author;

    private final long assignedAt = System.currentTimeMillis(), expiresAt;

    public boolean isPermanent() {
        return expiresAt == -1L;
    }

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }

    public String getAuthorName() {
        String name = "...";

        if (author.equals(Constant.CONSOLE_UUID)) name = "CONSOLE";
        else if (Core.getProfileData().exists(author))
            name = Core.getProfileData().read(author, false).getName();

        return name;
    }
}
