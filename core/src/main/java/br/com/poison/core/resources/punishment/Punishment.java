package br.com.poison.core.resources.punishment;

import br.com.poison.core.profile.Profile;
import br.com.poison.core.resources.account.Account;
import br.com.poison.core.resources.punishment.category.PunishmentCategory;
import br.com.poison.core.resources.punishment.reason.PunishmentReason;
import br.com.poison.core.util.extra.StringUtils;
import br.com.poison.core.util.extra.TimeUtil;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
public class Punishment {

    private final String code;

    private final Account punished, author;

    private final String ipAddress;

    private final PunishmentCategory category;
    private final PunishmentReason reason;

    private final long expiresAt, appliedAt;

    private final boolean valid;

    public Punishment(Profile punished, UUID authorId, String authorName, String ipAddress, PunishmentCategory category, PunishmentReason reason) {
        this.code = StringUtils.generateExclusiveCode(5);

        this.punished = new Account(punished.getId(), punished.getName());
        this.author = new Account(authorId, authorName);

        this.ipAddress = ipAddress;

        this.category = category;
        this.reason = reason;

        this.expiresAt = TimeUtil.getTime(reason.getTime());

        this.appliedAt = System.currentTimeMillis();

        this.valid = true;
    }

    public boolean isPermanent() {
        return expiresAt == -1L;
    }

    public boolean hasExpired() {
        return expiresAt < System.currentTimeMillis();
    }
}
