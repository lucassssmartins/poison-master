package br.com.poison.core.profile.resources.rank;

import br.com.poison.core.profile.resources.rank.assignment.Assignment;
import br.com.poison.core.profile.resources.rank.category.RankCategory;
import br.com.poison.core.Constant;
import br.com.poison.core.Core;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@ToString
public class Rank {

    private final RankCategory category;
    private final Assignment assignment;

    private final UUID author;

    private final long expiresAt, assignedAt = System.currentTimeMillis();

    public boolean isPayment() {
        return category.ordinal() >= RankCategory.VENOM.ordinal() && category.ordinal() <= RankCategory.BETA.ordinal();
    }

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

    public String getName() {
        return category.getName();
    }

    public ChatColor getColor() {
        return category.getColor();
    }

    public String getColoredName() {
        return getColor() + getName();
    }


    public String getMagicColor() {
        return getColor() + "§k";
    }

    public String getPrefix() {
        return category.getPrefix();
    }
}
